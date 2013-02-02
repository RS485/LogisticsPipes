package logisticspipes.pipes.upgrades;

import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class UpgradeManager implements ISimpleInventoryEventHandler {
	
	private SimpleInventory inv = new SimpleInventory(9, "UpgradeInventory", 16);
	private IPipeUpgrade[] upgrades = new IPipeUpgrade[8];
	private CoreRoutedPipe pipe;
	private int securityDelay = 0;
	
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
		for(int i=0;i<inv.getSizeInventory() - 1;i++) {
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
	    for(int pipeSlot = 0; pipeSlot < 8; pipeSlot++){
	    	dummy.addRestrictedSlot(pipeSlot, inv, 8 + pipeSlot * 18, 18, LogisticsPipes.UpgradeItem.itemID);
	    }
    	dummy.addRestrictedSlot(8, inv, 8 + 8 * 18, 18, new ISlotCheck() {
			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				if(itemStack == null) return false;
				if(itemStack.itemID != LogisticsPipes.LogisticsItemCard.itemID) return false;
				if(itemStack.getItemDamage() != LogisticsItemCard.SEC_CARD) return false;
				return true;
			}
    	});
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

	public boolean tryIserting(EntityPlayer entityplayer) {
		if(MainProxy.isClient()) return false;
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.UpgradeItem.itemID) {
			for(int i=0;i<inv.getSizeInventory() - 1;i++) {
				ItemStack item = inv.getStackInSlot(i);
				if(item == null) {
					inv.setInventorySlotContents(i, entityplayer.getCurrentEquippedItem().splitStack(1));
					InventoryChanged(inv);
					return true;
				} else if(item.getItemDamage() == entityplayer.getCurrentEquippedItem().getItemDamage()) {
					if(item.stackSize < inv.getInventoryStackLimit()) {
						item.stackSize++;
						entityplayer.getCurrentEquippedItem().splitStack(1);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public UUID getSecurityID() {
		ItemStack stack = inv.getStackInSlot(8);
		if(stack == null) return null;
		if(stack.itemID != LogisticsPipes.LogisticsItemCard.itemID || stack.getItemDamage() != LogisticsItemCard.SEC_CARD) return null;
		if(!stack.hasTagCompound()) return null;
		if(!stack.getTagCompound().hasKey("UUID")) return null;
		return UUID.fromString(stack.getTagCompound().getString("UUID"));
	}

	public void insetSecurityID(UUID id) {
		ItemStack stack = new ItemStack(LogisticsPipes.LogisticsItemCard, 1, LogisticsItemCard.SEC_CARD);
		stack.setTagCompound(new NBTTagCompound("tag"));
		stack.getTagCompound().setString("UUID", id.toString());
		inv.setInventorySlotContents(8, stack);
	}
	
	public void securityTick() {
		UUID id;
		if((id = getSecurityID()) != null) {
			TileEntity station = SimpleServiceLocator.securityStationManager.getStation(id);
			if(station == null) {
				securityDelay++;
			} else {
				securityDelay = 0;
			}
			if(securityDelay > 20) {
				inv.setInventorySlotContents(8, null);
			}
		}
	}
}
