package logisticspipes.pipes.upgrades;

import java.util.EnumSet;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.guis.pipe.UpgradeManagerGui;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.power.IC2PowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.RFPowerSupplierUpgrade;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.item.SimpleStackInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;

public class UpgradeManager implements ISimpleInventoryEventHandler, ISlotUpgradeManager, IPipeUpgradeManager {

	@Getter
	private SimpleStackInventory inv = new SimpleStackInventory(9, "UpgradeInventory", 16);
	@Getter
	private SimpleStackInventory sneakyInv = new SimpleStackInventory(9, "SneakyUpgradeInventory", 1);
	@Getter
	private SimpleStackInventory secInv = new SimpleStackInventory(1, "SecurityInventory", 16);
	private IPipeUpgrade[] upgrades = new IPipeUpgrade[9];
	private IPipeUpgrade[] sneakyUpgrades = new IPipeUpgrade[9];
	private CoreRoutedPipe pipe;
	private int securityDelay = 0;

	/* cached attributes */
	private ForgeDirection sneakyOrientation = ForgeDirection.UNKNOWN;
	private ForgeDirection[] combinedSneakyOrientation = new ForgeDirection[9];
	private int speedUpgradeCount = 0;
	private final EnumSet<ForgeDirection> disconnectedSides = EnumSet.noneOf(ForgeDirection.class);
	private boolean isAdvancedCrafter = false;
	private boolean isFuzzyUpgrade = false;
	private boolean isCombinedSneakyUpgrade = false;
	private int liquidCrafter = 0;
	private boolean hasByproductExtractor = false;
	private UUID uuid = null;
	private String uuidS = null;
	private boolean hasPatternUpgrade = false;
	private boolean hasPowerPassUpgrade = false;
	private boolean hasRFPowerUpgrade = false;
	private int getIC2PowerLevel = 0;
	private boolean hasCCRemoteControlUpgrade = false;
	private boolean hasCraftingMonitoringUpgrade = false;
	private boolean hasOpaqueUpgrade = false;
	private int craftingCleanup = 0;
	private boolean hasLogicControll = false;
	private boolean hasUpgradeModuleUpgarde = false;

	private boolean needsContainerPositionUpdate = false;

	public UpgradeManager(CoreRoutedPipe pipe) {
		this.pipe = pipe;
		inv.addListener(this);
		sneakyInv.addListener(this);
		secInv.addListener(this);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		inv.readFromNBT(nbttagcompound, "UpgradeInventory_");
		sneakyInv.readFromNBT(nbttagcompound, "SneakyUpgradeInventory_");
		secInv.readFromNBT(nbttagcompound, "SecurityInventory_");

		if (sneakyInv.getStackInSlot(8) != null) {
			if (sneakyInv.getStackInSlot(8).getItem() == LogisticsPipes.LogisticsItemCard && sneakyInv.getStackInSlot(8).getItemDamage() == LogisticsItemCard.SEC_CARD) {
				secInv.setInventorySlotContents(0, sneakyInv.getStackInSlot(8));
				sneakyInv.setInventorySlotContents(8, null);
			}
		}

		InventoryChanged(inv);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		inv.writeToNBT(nbttagcompound, "UpgradeInventory_");
		sneakyInv.writeToNBT(nbttagcompound, "SneakyUpgradeInventory_");
		secInv.writeToNBT(nbttagcompound, "SecurityInventory_");
		InventoryChanged(inv);
	}

	private boolean updateModule(int slot, IPipeUpgrade[] upgrades, IInventory inv) {
		upgrades[slot] = LogisticsPipes.UpgradeItem.getUpgradeForItem(inv.getStackInSlot(slot), upgrades[slot]);
		if (upgrades[slot] == null) {
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
	public void InventoryChanged(IInventory inventory) {
		boolean needUpdate = false;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (item != null) {
				needUpdate |= updateModule(i, upgrades, inv);
			} else if (item == null && upgrades[i] != null) {
				needUpdate |= removeUpgrade(i, upgrades);
			}
		}
		//update sneaky direction, speed upgrade count and disconnection
		sneakyOrientation = ForgeDirection.UNKNOWN;
		speedUpgradeCount = 0;
		isAdvancedCrafter = false;
		isFuzzyUpgrade = false;
		boolean combinedBuffer = isCombinedSneakyUpgrade;
		isCombinedSneakyUpgrade = false;
		liquidCrafter = 0;
		disconnectedSides.clear();
		hasByproductExtractor = false;
		hasPatternUpgrade = false;
		hasPowerPassUpgrade = false;
		hasRFPowerUpgrade = false;
		getIC2PowerLevel = 0;
		hasCCRemoteControlUpgrade = false;
		hasCraftingMonitoringUpgrade = false;
		hasOpaqueUpgrade = false;
		craftingCleanup = 0;
		hasLogicControll = false;
		hasUpgradeModuleUpgarde = false;
		for (int i = 0; i < upgrades.length; i++) {
			IPipeUpgrade upgrade = upgrades[i];
			if (upgrade instanceof SneakyUpgrade && sneakyOrientation == ForgeDirection.UNKNOWN && !isCombinedSneakyUpgrade) {
				sneakyOrientation = ((SneakyUpgrade) upgrade).getSneakyOrientation();
			} else if (upgrade instanceof SpeedUpgrade) {
				speedUpgradeCount += inv.getStackInSlot(i).stackSize;
			} else if (upgrade instanceof ConnectionUpgrade) {
				disconnectedSides.add(((ConnectionUpgrade) upgrade).getSide());
			} else if (upgrade instanceof AdvancedSatelliteUpgrade) {
				isAdvancedCrafter = true;
			} else if (upgrade instanceof FuzzyUpgrade) {
				isFuzzyUpgrade = true;
			} else if (upgrade instanceof CombinedSneakyUpgrade && sneakyOrientation == ForgeDirection.UNKNOWN) {
				isCombinedSneakyUpgrade = true;
			} else if (upgrade instanceof FluidCraftingUpgrade) {
				liquidCrafter += inv.getStackInSlot(i).stackSize;
			} else if (upgrade instanceof CraftingByproductUpgrade) {
				hasByproductExtractor = true;
			} else if (upgrade instanceof PatternUpgrade) {
				hasPatternUpgrade = true;
			} else if (upgrade instanceof PowerTransportationUpgrade) {
				hasPowerPassUpgrade = true;
			} else if (upgrade instanceof RFPowerSupplierUpgrade) {
				hasRFPowerUpgrade = true;
			} else if (upgrade instanceof IC2PowerSupplierUpgrade) {
				getIC2PowerLevel = Math.max(getIC2PowerLevel, ((IC2PowerSupplierUpgrade) upgrade).getPowerLevel());
			} else if (upgrade instanceof CCRemoteControlUpgrade) {
				hasCCRemoteControlUpgrade = true;
			} else if (upgrade instanceof CraftingMonitoringUpgrade) {
				hasCraftingMonitoringUpgrade = true;
			} else if (upgrade instanceof OpaqueUpgrade) {
				hasOpaqueUpgrade = true;
			} else if (upgrade instanceof CraftingCleanupUpgrade) {
				craftingCleanup += inv.getStackInSlot(i).stackSize;
			} else if (upgrade instanceof LogicControllerUpgrade) {
				hasLogicControll = true;
			} else if (upgrade instanceof UpgradeModuleUpgrade) {
				hasUpgradeModuleUpgarde = true;
			}
		}
		liquidCrafter = Math.min(liquidCrafter, ItemUpgrade.MAX_LIQUID_CRAFTER);
		craftingCleanup = Math.min(craftingCleanup, ItemUpgrade.MAX_CRAFTING_CLEANUP);
		if (combinedBuffer != isCombinedSneakyUpgrade) {
			needsContainerPositionUpdate = true;
		}
		for (int i = 0; i < sneakyInv.getSizeInventory() - 1; i++) {
			ItemStack item = sneakyInv.getStackInSlot(i);
			if (item != null) {
				needUpdate |= updateModule(i, sneakyUpgrades, sneakyInv);
			} else if (item == null && sneakyUpgrades[i] != null) {
				needUpdate |= removeUpgrade(i, sneakyUpgrades);
			}
		}
		for (int i = 0; i < sneakyUpgrades.length; i++) {
			IPipeUpgrade upgrade = sneakyUpgrades[i];
			if (upgrade instanceof SneakyUpgrade) {
				combinedSneakyOrientation[i] = ((SneakyUpgrade) upgrade).getSneakyOrientation();
			}
		}
		if (needUpdate) {
			pipe.connectionUpdate();
			if (pipe.container != null) {
				pipe.container.sendUpdateToClient();
			}
		}
		uuid = null;
		uuidS = null;
		ItemStack stack = secInv.getStackInSlot(0);
		if (stack == null) {
			return;
		}
		if (stack.getItem() != LogisticsPipes.LogisticsItemCard || stack.getItemDamage() != LogisticsItemCard.SEC_CARD) {
			return;
		}
		if (!stack.hasTagCompound()) {
			return;
		}
		if (!stack.getTagCompound().hasKey("UUID")) {
			return;
		}
		uuid = UUID.fromString(stack.getTagCompound().getString("UUID"));
		uuidS = uuid.toString();
	}

	/* Special implementations */

	@Override
	public boolean hasSneakyUpgrade() {
		return sneakyOrientation != ForgeDirection.UNKNOWN;
	}

	@Override
	public ForgeDirection getSneakyOrientation() {
		return sneakyOrientation;
	}

	@Override
	public int getSpeedUpgradeCount() {
		return speedUpgradeCount;
	}

	@Override
	public boolean hasCombinedSneakyUpgrade() {
		return isCombinedSneakyUpgrade;
	}

	@Override
	public ForgeDirection[] getCombinedSneakyOrientation() {
		return combinedSneakyOrientation;
	}

	public void openGui(EntityPlayer entityplayer, CoreRoutedPipe pipe) {
		NewGuiHandler.getGui(UpgradeManagerGui.class).setTilePos(pipe.container).open(entityplayer);
	}

	public IGuiOpenControler getGuiController() {
		return new IGuiOpenControler() {

			PlayerCollectionList players = new PlayerCollectionList();

			@Override
			public void guiOpenedByPlayer(EntityPlayer player) {
				players.add(player);
			}

			@Override
			public void guiClosedByPlayer(EntityPlayer player) {
				players.remove(player);
				if (players.isEmpty() && !isCombinedSneakyUpgrade) {
					sneakyInv.dropContents(pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
				}
			}
		};
	}

	public DummyContainer getDummyContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, inv, getGuiController());
		dummy.addNormalSlotsForPlayerInventory(8, isCombinedSneakyUpgrade ? 90 : 60);

		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 8; pipeSlot++) {
			dummy.addRestrictedSlot(pipeSlot, inv, 8 + pipeSlot * 18, 18, new ISlotCheck() {

				@Override
				public boolean isStackAllowed(ItemStack itemStack) {
					if (itemStack == null) {
						return false;
					}
					if (itemStack.getItem() == LogisticsPipes.UpgradeItem) {
						if (!LogisticsPipes.UpgradeItem.getUpgradeForItem(itemStack, null).isAllowedForPipe(pipe)) {
							return false;
						}
					} else {
						return false;
					}
					return true;
				}
			});
		}
		//Static slot for Security Cards
		dummy.addStaticRestrictedSlot(0, secInv, 8 + 8 * 18, 18, new ISlotCheck() {

			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				if (itemStack == null) {
					return false;
				}
				if (itemStack.getItem() != LogisticsPipes.LogisticsItemCard) {
					return false;
				}
				if (itemStack.getItemDamage() != LogisticsItemCard.SEC_CARD) {
					return false;
				}
				if (!SimpleServiceLocator.securityStationManager.isAuthorized(UUID.fromString(itemStack.getTagCompound().getString("UUID")))) {
					return false;
				}
				return true;
			}
		}, 1);

		int y = isCombinedSneakyUpgrade ? 58 : 100000;
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addRestrictedSlot(pipeSlot, sneakyInv, 8 + pipeSlot * 18, y, new ISlotCheck() {

				@Override
				public boolean isStackAllowed(ItemStack itemStack) {
					if (itemStack == null) {
						return false;
					}
					if (itemStack.getItem() == LogisticsPipes.UpgradeItem) {
						IPipeUpgrade upgrade = LogisticsPipes.UpgradeItem.getUpgradeForItem(itemStack, null);
						if (!(upgrade instanceof SneakyUpgrade)) {
							return false;
						}
						if (!upgrade.isAllowedForPipe(pipe)) {
							return false;
						}
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
		sneakyInv.dropContents(pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
	}

	@Override
	public boolean isSideDisconnected(ForgeDirection side) {
		return disconnectedSides.contains(side);
	}

	public boolean tryIserting(World world, EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.UpgradeItem) {
			if (MainProxy.isClient(world)) {
				return true;
			}
			IPipeUpgrade upgrade = LogisticsPipes.UpgradeItem.getUpgradeForItem(entityplayer.getCurrentEquippedItem(), null);
			if (upgrade.isAllowedForPipe(pipe)) {
				if (isCombinedSneakyUpgrade) {
					if (upgrade instanceof SneakyUpgrade) {
						if (insertIntInv(entityplayer, sneakyInv)) {
							return true;
						}
					}
				}
				if (insertIntInv(entityplayer, inv)) {
					return true;
				}
			}
		}
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsItemCard && entityplayer.getCurrentEquippedItem().getItemDamage() == LogisticsItemCard.SEC_CARD) {
			if (MainProxy.isClient(world)) {
				return true;
			}
			if (secInv.getStackInSlot(0) == null) {
				ItemStack newItem = entityplayer.getCurrentEquippedItem().splitStack(1);
				secInv.setInventorySlotContents(0, newItem);
				InventoryChanged(secInv);
				return true;
			}
		}
		return false;
	}

	private boolean insertIntInv(EntityPlayer entityplayer, SimpleStackInventory inv) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (item == null) {
				inv.setInventorySlotContents(i, entityplayer.getCurrentEquippedItem().splitStack(1));
				InventoryChanged(inv);
				return true;
			} else if (item.getItemDamage() == entityplayer.getCurrentEquippedItem().getItemDamage()) {
				if (item.stackSize < inv.getInventoryStackLimit()) {
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
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString("UUID", id.toString());
		secInv.setInventorySlotContents(0, stack);
		InventoryChanged(secInv);
	}

	public void securityTick() {
		if ((getSecurityID()) != null) {
			if (!SimpleServiceLocator.securityStationManager.isAuthorized(uuidS)) {
				securityDelay++;
			} else {
				securityDelay = 0;
			}
			if (securityDelay > 20) {
				secInv.clearInventorySlotContents(0);
				InventoryChanged(secInv);
			}
		}
	}

	@Override
	public boolean isAdvancedSatelliteCrafter() {
		return isAdvancedCrafter;
	}

	@Override
	public boolean isFuzzyUpgrade() {
		return isFuzzyUpgrade;
	}

	@Override
	public int getFluidCrafter() {
		return liquidCrafter;
	}

	@Override
	public boolean hasByproductExtractor() {
		return hasByproductExtractor;
	}

	@Override
	public boolean hasPatternUpgrade() {
		return hasPatternUpgrade;
	}

	@Override
	public boolean hasPowerPassUpgrade() {
		return hasPowerPassUpgrade || hasRFPowerUpgrade || getIC2PowerLevel > 0;
	}

	@Override
	public boolean hasRFPowerSupplierUpgrade() {
		return hasRFPowerUpgrade;
	}

	@Override
	public int getIC2PowerLevel() {
		return getIC2PowerLevel;
	}

	@Override
	public boolean hasCCRemoteControlUpgrade() {
		return hasCCRemoteControlUpgrade;
	}

	@Override
	public boolean hasCraftingMonitoringUpgrade() {
		return hasCraftingMonitoringUpgrade;
	}

	@Override
	public boolean isOpaque() {
		return hasOpaqueUpgrade;
	}

	@Override
	public int getCrafterCleanup() {
		return craftingCleanup;
	}

	public boolean hasLogicControll() {
		return hasLogicControll;
	}

	@Override
	public boolean hasUpgradeModuleUpgrade() {
		return hasUpgradeModuleUpgarde;
	}

	@Override
	public boolean hasOwnSneakyUpgrade() {
		return false;
	}
}
