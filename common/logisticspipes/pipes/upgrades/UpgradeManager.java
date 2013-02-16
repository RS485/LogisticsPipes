package logisticspipes.pipes.upgrades;

import java.util.EnumSet;
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

	/* cached attributes */
	private ForgeDirection sneakyOrientation = ForgeDirection.UNKNOWN;
	private int speedUpgradeCount = 0;
	private final EnumSet<ForgeDirection> disconnectedSides = EnumSet.noneOf(ForgeDirection.class);
	
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

	private boolean updateModule(int slot) {
		upgrades[slot] = LogisticsPipes.UpgradeItem.getUpgradeForItem(inv.getStackInSlot(slot), upgrades[slot]);
		return upgrades[slot].needsUpdate();
	}
	
	private boolean removeUpgrade(int slot) {
		boolean needUpdate = upgrades[slot].needsUpdate();
		upgrades[slot] = null;
		return needUpdate;
	}
	
	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		boolean needUpdate = false;
		for(int i=0;i<inv.getSizeInventory() - 1;i++) {
			ItemStack item = inv.getStackInSlot(i);
			if(item != null) {
				needUpdate |= updateModule(i);
			} else if(item == null && upgrades[i] != null) {
				needUpdate |= removeUpgrade(i);
			}
		}
		//update sneaky direction, speed upgrade count and disconnection
		sneakyOrientation = ForgeDirection.UNKNOWN;
		speedUpgradeCount = 0;
		disconnectedSides.clear();
		for(int i=0;i<upgrades.length;i++) {
			IPipeUpgrade upgrade = upgrades[i];
			if(upgrade instanceof SneakyUpgrade && sneakyOrientation == ForgeDirection.UNKNOWN) {
				sneakyOrientation = ((SneakyUpgrade) upgrade).getSneakyOrientation();
			} else if(upgrade instanceof SpeedUpgrade) {
				speedUpgradeCount += inv.getStackInSlot(i).stackSize;
			} else if(upgrade instanceof ConnectionUpgrade) {
				disconnectedSides.add(((ConnectionUpgrade)upgrade).getSide());
			}
		}
		if(needUpdate) {
			pipe.connectionUpdate();
		}
	}

	/* Special implementations */
	
	public boolean hasSneakyUpgrade() {
		return sneakyOrientation != ForgeDirection.UNKNOWN;
	}

	public ForgeDirection getSneakyOrientation() {
		return sneakyOrientation;
	}
	
	public int getSpeedUpgradeCount() {
		return speedUpgradeCount;
	}

	public void openGui(EntityPlayer entityplayer, CoreRoutedPipe pipe) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Upgrade_Manager, pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
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
		return disconnectedSides.contains(side);
	}

	public boolean tryIserting(World world, EntityPlayer entityplayer) {
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.UpgradeItem.itemID) {
			if(MainProxy.isClient(world)) return true;
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
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.LogisticsItemCard.itemID && entityplayer.getCurrentEquippedItem().getItemDamage() == LogisticsItemCard.SEC_CARD) {
			if(MainProxy.isClient(world)) return true;
			if(inv.getStackInSlot(8) == null) {
				inv.setInventorySlotContents(8, entityplayer.getCurrentEquippedItem().copy());
				inv.getStackInSlot(8).stackSize = 1;
				entityplayer.getCurrentEquippedItem().splitStack(1);
				return true;
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
