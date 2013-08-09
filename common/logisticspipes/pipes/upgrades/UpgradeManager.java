package logisticspipes.pipes.upgrades;

import java.util.EnumSet;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class UpgradeManager implements ISimpleInventoryEventHandler {

	private SimpleInventory inv = new SimpleInventory(9, "UpgradeInventory", 16);
	private SimpleInventory sneakyInv = new SimpleInventory(9, "SneakyUpgradeInventory", 1);
	private IPipeUpgrade[] upgrades = new IPipeUpgrade[8];
	private IPipeUpgrade[] sneakyUpgrades = new IPipeUpgrade[9];
	private CoreRoutedPipe pipe;
	private int securityDelay = 0;

	/* cached attributes */
	private ForgeDirection sneakyOrientation = ForgeDirection.UNKNOWN;
	private ForgeDirection[] combinedSneakyOrientation = new ForgeDirection[9];
	private int speedUpgradeCount = 0;
	private final EnumSet<ForgeDirection> disconnectedSides = EnumSet.noneOf(ForgeDirection.class);
	private boolean isAdvancedCrafter = false;
	private boolean isCombinedSneakyUpgrade = false;
	private int liquidCrafter = 0;
	private boolean hasByproductExtractor = false;
	private UUID uuid = null;
	private String uuidS = null;
	
	private boolean needsContainerPositionUpdate = false;
	
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

	private boolean updateModule(int slot, IPipeUpgrade[] upgrades, IInventory inv) {
		upgrades[slot] = LogisticsPipes.UpgradeItem.getUpgradeForItem(inv.getStackInSlot(slot), upgrades[slot]);
		if(upgrades[slot] == null) {
			inv.setInventorySlotContents(slot, null);
			return false;
		} else {
			return upgrades[slot].needsUpdate();
		}
	}
	
	private boolean removeUpgrade(int slot, IPipeUpgrade[] upgrades) {
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
				needUpdate |= updateModule(i, upgrades, inv);
			} else if(item == null && upgrades[i] != null) {
				needUpdate |= removeUpgrade(i, upgrades);
			}
		}
		//update sneaky direction, speed upgrade count and disconnection
		sneakyOrientation = ForgeDirection.UNKNOWN;
		speedUpgradeCount = 0;
		isAdvancedCrafter = false;
		boolean combinedBuffer = isCombinedSneakyUpgrade;
		isCombinedSneakyUpgrade = false;
		liquidCrafter = 0;
		disconnectedSides.clear();
		hasByproductExtractor = false;
		for(int i=0;i<upgrades.length;i++) {
			IPipeUpgrade upgrade = upgrades[i];
			if(upgrade instanceof SneakyUpgrade && sneakyOrientation == ForgeDirection.UNKNOWN && !isCombinedSneakyUpgrade) {
				sneakyOrientation = ((SneakyUpgrade) upgrade).getSneakyOrientation();
			} else if(upgrade instanceof SpeedUpgrade) {
				speedUpgradeCount += inv.getStackInSlot(i).stackSize;
			} else if(upgrade instanceof ConnectionUpgrade) {
				disconnectedSides.add(((ConnectionUpgrade)upgrade).getSide());
			} else if(upgrade instanceof AdvancedSatelliteUpgrade) {
				isAdvancedCrafter = true;
			} else if(upgrade instanceof CombinedSneakyUpgrade && sneakyOrientation == ForgeDirection.UNKNOWN) {
				isCombinedSneakyUpgrade = true;
			} else if(upgrade instanceof FluidCraftingUpgrade) {
				liquidCrafter += inv.getStackInSlot(i).stackSize;
			} else if(upgrade instanceof CraftingByproductUpgrade) {
				hasByproductExtractor = true;
			}
		}
		liquidCrafter = Math.min(liquidCrafter, ItemUpgrade.MAX_LIQUID_CRAFTER);
		if(combinedBuffer != isCombinedSneakyUpgrade) {
			needsContainerPositionUpdate = true;
		}
		for(int i=0;i<sneakyInv.getSizeInventory() - 1;i++) {
			ItemStack item = sneakyInv.getStackInSlot(i);
			if(item != null) {
				needUpdate |= updateModule(i, sneakyUpgrades, sneakyInv);
			} else if(item == null && sneakyUpgrades[i] != null) {
				needUpdate |= removeUpgrade(i, sneakyUpgrades);
			}
		}
		for(int i=0;i<sneakyUpgrades.length;i++) {
			IPipeUpgrade upgrade = sneakyUpgrades[i];
			if(upgrade instanceof SneakyUpgrade) {
				combinedSneakyOrientation[i] = ((SneakyUpgrade) upgrade).getSneakyOrientation();
			}
		}
		if(needUpdate) {
			pipe.connectionUpdate();
		}
		uuid = null;
		uuidS = null;
		ItemStack stack = inv.getStackInSlot(8);
		if(stack == null) return;
		if(stack.itemID != LogisticsPipes.LogisticsItemCard.itemID || stack.getItemDamage() != LogisticsItemCard.SEC_CARD) return;
		if(!stack.hasTagCompound()) return;
		if(!stack.getTagCompound().hasKey("UUID")) return;
		uuid = UUID.fromString(stack.getTagCompound().getString("UUID"));
		uuidS = uuid.toString();
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
	
	public boolean hasCombinedSneakyUpgrade() {
		return isCombinedSneakyUpgrade;
	}

	public ForgeDirection[] getCombinedSneakyOrientation() {
		return combinedSneakyOrientation;
	}

	public void openGui(EntityPlayer entityplayer, CoreRoutedPipe pipe) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Upgrade_Manager, pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
	}

	public DummyContainer getDummyContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, inv, new IGuiOpenControler() {
			PlayerCollectionList players = new PlayerCollectionList();
			@Override
			public void guiOpenedByPlayer(EntityPlayer player) {
				players.add(player);
			}
			@Override
			public void guiClosedByPlayer(EntityPlayer player) {
				players.remove(player);
				if(players.isEmpty() && !isCombinedSneakyUpgrade) {
					sneakyInv.dropContents(pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
				}
			}
		});
		dummy.addNormalSlotsForPlayerInventory(8, isCombinedSneakyUpgrade ? 90 : 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 8; pipeSlot++){
	    	dummy.addRestrictedSlot(pipeSlot, inv, 8 + pipeSlot * 18, 18, new ISlotCheck() {
				@Override
				public boolean isStackAllowed(ItemStack itemStack) {
					if(itemStack == null) return false;
					if(itemStack.itemID == LogisticsPipes.UpgradeItem.itemID) {
						if(!LogisticsPipes.UpgradeItem.getUpgradeForItem(itemStack, null).isAllowed(pipe)) return false;
					} else {
						return false;
					}
					return true;
				}
	    	});
	    }
	    //Static slot for Security Cards
    	dummy.addStaticRestrictedSlot(8, inv, 8 + 8 * 18, 18, new ISlotCheck() {
			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				if(itemStack == null) return false;
				if(itemStack.itemID != LogisticsPipes.LogisticsItemCard.itemID) return false;
				if(itemStack.getItemDamage() != LogisticsItemCard.SEC_CARD) return false;
				if(!SimpleServiceLocator.securityStationManager.isAuthorized(UUID.fromString(itemStack.getTagCompound().getString("UUID")))) return false;
				return true;
			}
    	}, 1);
    	
		int y = isCombinedSneakyUpgrade ? 58 : 100000;
		for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addRestrictedSlot(pipeSlot, sneakyInv, 8 + pipeSlot * 18, y, new ISlotCheck() {
				@Override
				public boolean isStackAllowed(ItemStack itemStack) {
					if(itemStack == null) return false;
					if(itemStack.itemID == LogisticsPipes.UpgradeItem.itemID) {
						IPipeUpgrade upgrade = LogisticsPipes.UpgradeItem.getUpgradeForItem(itemStack, null);
						if(!(upgrade instanceof SneakyUpgrade)) return false;
						if(!upgrade.isAllowed(pipe)) return false;
					} else {
						return false;
					}
					return true;
				}
	    	});
	    }
	    return dummy;
	}
	
	public boolean isNeedingContainerUpdate() {
		boolean tmp = needsContainerPositionUpdate;
		needsContainerPositionUpdate = false;
		return tmp;
	}
	
	public void dropUpgrades() {
		inv.dropContents(pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
	}

	public boolean isSideDisconnected(ForgeDirection side) {
		return disconnectedSides.contains(side);
	}

	public boolean tryIserting(World world, EntityPlayer entityplayer) {
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.UpgradeItem.itemID) {
			if(MainProxy.isClient(world)) return true;
			IPipeUpgrade upgrade = LogisticsPipes.UpgradeItem.getUpgradeForItem(entityplayer.getCurrentEquippedItem(), null);
			if(upgrade.isAllowed(pipe)) {
				if(isCombinedSneakyUpgrade) {
					if(upgrade instanceof SneakyUpgrade) {
						if(insertIntInv(entityplayer, sneakyInv, 0)) return true;
					}
				}
				if(insertIntInv(entityplayer, inv, 1)) return true;
			}
		}
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.LogisticsItemCard.itemID && entityplayer.getCurrentEquippedItem().getItemDamage() == LogisticsItemCard.SEC_CARD) {
			if(MainProxy.isClient(world)) return true;
			if(inv.getStackInSlot(8) == null) {
				ItemStack newItem=entityplayer.getCurrentEquippedItem().splitStack(1);
				inv.setInventorySlotContents(8, newItem);
				InventoryChanged(inv);
				return true;
			}
		}
		return false;
	}
	
	private boolean insertIntInv(EntityPlayer entityplayer, SimpleInventory inv, int sub) {
		for(int i=0;i<inv.getSizeInventory() - sub;i++) {
			ItemStack item = inv.getStackInSlot(i);
			if(item == null) {
				inv.setInventorySlotContents(i, entityplayer.getCurrentEquippedItem().splitStack(1));
				InventoryChanged(inv);
				return true;
			} else if(item.getItemDamage() == entityplayer.getCurrentEquippedItem().getItemDamage()) {
				if(item.stackSize < inv.getInventoryStackLimit()) {
					item.stackSize++;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					inv.setInventorySlotContents(i, item);
					InventoryChanged(inv);
					return true;
				}
			}
		}
		return false;
	}
	
	public UUID getSecurityID() {
		return uuid;
	}

	public void insetSecurityID(UUID id) {
		ItemStack stack = new ItemStack(LogisticsPipes.LogisticsItemCard, 1, LogisticsItemCard.SEC_CARD);
		stack.setTagCompound(new NBTTagCompound("tag"));
		stack.getTagCompound().setString("UUID", id.toString());
		inv.setInventorySlotContents(8, stack);
		InventoryChanged(inv);
	}
	
	public void securityTick() {
		if((getSecurityID()) != null) {
			if(!SimpleServiceLocator.securityStationManager.isAuthorized(uuidS)) {
				securityDelay++;
			} else {
				securityDelay = 0;
			}
			if(securityDelay > 20) {
				inv.clearInventorySlotContents(8);
			}
		}
	}
	
	public boolean isAdvancedSatelliteCrafter() {
		return isAdvancedCrafter;
	}
	
	public int getFluidCrafter() {
		return liquidCrafter;
	}
	
	public boolean hasByproductExtractor() {
		return hasByproductExtractor;
	}
}
