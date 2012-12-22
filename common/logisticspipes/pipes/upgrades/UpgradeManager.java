package logisticspipes.pipes.upgrades;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class UpgradeManager implements ISimpleInventoryEventHandler {
	
	private SimpleInventory inv = new SimpleInventory(9, "UpgradeInventory", 16);
	private IPipeUpgrade[] upgrades = new IPipeUpgrade[9];
	private CoreRoutedPipe pipe;
	
	public UpgradeManager(CoreRoutedPipe pipe) {
		this.pipe = pipe;
		inv.addListener(this);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		inv.readFromNBT(nbttagcompound, "UpgradeInventory_");
		InventoryChanged(inv);
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		inv.writeToNBT(nbttagcompound, "UpgradeInventory_");
		InventoryChanged(inv);
	}

	private void updateModule(int slot) {
		upgrades[slot] = LogisticsPipes.UpgradeItem.getUpgradeForItem(inv.getStackInSlot(slot), upgrades[slot]);
		if(upgrades[slot].needsUpdate()) {
			pipe.connectionUpdate();
		}
	}
	
	private void removeUpgrade(int slot) {
		boolean needUpdate = false;
		if(upgrades[slot].needsUpdate()) {
			needUpdate = true;
		}
		upgrades[slot] = null;
		if(needUpdate) {
			pipe.connectionUpdate();
		}
	}
	
	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		for(int i=0;i<inv.getSizeInventory();i++) {
			ItemStack item = inv.getStackInSlot(i);
			if(item != null) {
				updateModule(i);
			} else if(item == null && upgrades[i] != null) {
				removeUpgrade(i);
			}
		}
	}

	/* Special implementations */
	
	public boolean hasSneakyUpgrade() {
		return getSneakyUpgrade() != null;
	}

	public SneakyUpgrade getSneakyUpgrade() {
		for(int i=0;i<upgrades.length;i++) {
			IPipeUpgrade update = upgrades[i];
			if(update instanceof SneakyUpgrade) {
				return (SneakyUpgrade) update;
			}
		}
		return null;
	}
	
	public int getSpeedUpgradeCount() {
		int count = 0;
		for(int i=0;i<upgrades.length;i++) {
			IPipeUpgrade update = upgrades[i];
			if(update instanceof SpeedUpgrade) {
				count += inv.getStackInSlot(i).stackSize;
			}
		}
		return count;
	}

	public boolean openGui(EntityPlayer entityplayer, CoreRoutedPipe pipe) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Upgrade_Manager, pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
		return true;
	}

	public DummyContainer getDummyContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player.inventory, inv);
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addRestrictedSlot(pipeSlot, inv, 8 + pipeSlot * 18, 18, LogisticsPipes.UpgradeItem.shiftedIndex);
	    }
	    return dummy;
	}
	
	public void dropUpgrades(World worldObj, int xCoord, int yCoord, int zCoord) {
		inv.dropContents(worldObj, xCoord, yCoord, zCoord);
	}

	public boolean isSideDisconnected(ForgeDirection side) {
		for(int i=0;i<upgrades.length;i++) {
			IPipeUpgrade upgrade = upgrades[i];
			if(upgrade instanceof ConnectionUpgrade) {
				if(((ConnectionUpgrade)upgrade).getSide() == side) {
					return true;
				}
			}
		}
		return false;
	}
}
