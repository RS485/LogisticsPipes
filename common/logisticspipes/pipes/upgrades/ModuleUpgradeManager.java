package logisticspipes.pipes.upgrades;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import lombok.Getter;

import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.item.SimpleStackInventory;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class ModuleUpgradeManager implements ISimpleInventoryEventHandler, ISlotUpgradeManager {

	private final UpgradeManager parent;
	@Getter
	private final SimpleStackInventory inv = new SimpleStackInventory(2, "UpgradeInventory", 16);
	private final IPipeUpgrade[] upgrades = new IPipeUpgrade[2];
	private final PipeLogisticsChassis pipe;

	private EnumFacing sneakyOrientation = null;
	private boolean isAdvancedCrafter = false;
	private boolean isFuzzyUpgrade = false;
	private int liquidCrafter = 0;
	private boolean hasByproductExtractor = false;
	private boolean hasPatternUpgrade = false;
	private int craftingCleanup = 0;
	private int actionSpeedUpgrade = 0;
	private int itemExtractionUpgrade = 0;
	private int itemStackExtractionUpgrade = 0;

	private boolean[] guiUpgrades = new boolean[2];

	public ModuleUpgradeManager(PipeLogisticsChassis pipe, UpgradeManager parent) {
		this.pipe = pipe;
		this.parent = parent;
		inv.addListener(this);
	}

	@Override
	public boolean hasPatternUpgrade() {
		return hasPatternUpgrade || parent.hasPatternUpgrade();
	}

	@Override
	public boolean isAdvancedSatelliteCrafter() {
		return isAdvancedCrafter || parent.isAdvancedSatelliteCrafter();
	}

	@Override
	public boolean hasByproductExtractor() {
		return hasByproductExtractor || parent.hasByproductExtractor();
	}

	@Override
	public int getFluidCrafter() {
		return Math.min(liquidCrafter + parent.getFluidCrafter(), ItemUpgrade.MAX_LIQUID_CRAFTER);
	}

	@Override
	public boolean isFuzzyUpgrade() {
		return isFuzzyUpgrade || parent.isFuzzyUpgrade();
	}

	@Override
	public int getCrafterCleanup() {
		return Math.min(craftingCleanup + parent.getCrafterCleanup(), ItemUpgrade.MAX_CRAFTING_CLEANUP);
	}

	@Override
	public boolean hasSneakyUpgrade() {
		if (sneakyOrientation != null) {
			return true;
		}
		return parent.hasSneakyUpgrade();
	}

	@Override
	public EnumFacing getSneakyOrientation() {
		if (sneakyOrientation != null) {
			return sneakyOrientation;
		}
		return parent.getSneakyOrientation();
	}

	@Override
	public boolean hasOwnSneakyUpgrade() {
		return sneakyOrientation != null;
	}

	@Override
	public IPipeUpgrade getUpgrade(int slot) {
		return upgrades[slot];
	}

	@Override
	public DoubleCoordinates getPipePosition() {
		return pipe.getLPPosition();
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		boolean needUpdate = false;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (item.isEmpty()) {
				if (upgrades[i] != null) {
					needUpdate |= removeUpgrade(i, upgrades);
				}
			} else {
				needUpdate |= updateModule(i, upgrades, inv);
			}
		}
		//update sneaky direction, speed upgrade count and disconnection
		sneakyOrientation = null;
		isAdvancedCrafter = false;
		isFuzzyUpgrade = false;
		liquidCrafter = 0;
		hasByproductExtractor = false;
		hasPatternUpgrade = false;
		craftingCleanup = 0;
		actionSpeedUpgrade = 0;
		itemExtractionUpgrade = 0;
		itemStackExtractionUpgrade = 0;
		guiUpgrades = new boolean[2];
		for (int i = 0; i < upgrades.length; i++) {
			IPipeUpgrade upgrade = upgrades[i];
			if (upgrade instanceof SneakyUpgradeConfig && sneakyOrientation == null) {
				ItemStack stack = inv.getStackInSlot(i);
				sneakyOrientation = ((SneakyUpgradeConfig) upgrade).getSide(stack);
			} else if (upgrade instanceof AdvancedSatelliteUpgrade) {
				isAdvancedCrafter = true;
			} else if (upgrade instanceof FuzzyUpgrade) {
				isFuzzyUpgrade = true;
			} else if (upgrade instanceof FluidCraftingUpgrade) {
				liquidCrafter += inv.getStackInSlot(i).getCount();
			} else if (upgrade instanceof CraftingByproductUpgrade) {
				hasByproductExtractor = true;
			} else if (upgrade instanceof PatternUpgrade) {
				hasPatternUpgrade = true;
			} else if (upgrade instanceof CraftingCleanupUpgrade) {
				craftingCleanup += inv.getStackInSlot(i).getCount();
			} else if (upgrade instanceof ActionSpeedUpgrade) {
				actionSpeedUpgrade += inv.getStackInSlot(i).getCount();
			} else if (upgrade instanceof ItemExtractionUpgrade) {
				itemExtractionUpgrade += inv.getStackInSlot(i).getCount();
			} else if (upgrade instanceof ItemStackExtractionUpgrade) {
				itemStackExtractionUpgrade += inv.getStackInSlot(i).getCount();
			}
			if (upgrade instanceof IConfigPipeUpgrade) {
				guiUpgrades[i] = true;
			}
		}
		liquidCrafter = Math.min(liquidCrafter, ItemUpgrade.MAX_LIQUID_CRAFTER);
		craftingCleanup = Math.min(craftingCleanup, ItemUpgrade.MAX_CRAFTING_CLEANUP);
		itemExtractionUpgrade = Math.min(itemExtractionUpgrade, ItemUpgrade.MAX_ITEM_EXTRACTION);
		itemStackExtractionUpgrade = Math.min(itemStackExtractionUpgrade, ItemUpgrade.MAX_ITEM_STACK_EXTRACTION);
		if (needUpdate) {
			MainProxy.runOnServer(null, () -> () -> {
				pipe.connectionUpdate();
				if (pipe.container != null) {
					pipe.container.sendUpdateToClient();
				}
			});
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		inv.readFromNBT(nbttagcompound, "ModuleUpgradeInventory_" + prefix);
		InventoryChanged(inv);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		inv.writeToNBT(nbttagcompound, "ModuleUpgradeInventory_" + prefix);
		InventoryChanged(inv);
	}

	private boolean updateModule(int slot, IPipeUpgrade[] upgrades, IInventory inv) {
		ItemStack stackInSlot = inv.getStackInSlot(slot);
		if (stackInSlot.isEmpty() || !(stackInSlot.getItem() instanceof ItemUpgrade)) {
			upgrades[slot] = null;
		} else {
			upgrades[slot] = ((ItemUpgrade) stackInSlot.getItem()).getUpgradeForItem(stackInSlot, upgrades[slot]);
		}
		if (upgrades[slot] == null) {
			inv.setInventorySlotContents(slot, ItemStack.EMPTY);
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

	public boolean hasGuiUpgrade(int i) {
		return guiUpgrades[i];
	}

	@Override
	public int getActionSpeedUpgrade() {
		return actionSpeedUpgrade;
	}

	@Override
	public int getItemExtractionUpgrade() {
		return itemExtractionUpgrade;
	}

	@Override
	public int getItemStackExtractionUpgrade() {
		return itemStackExtractionUpgrade;
	}

	public void dropUpgrades() {
		inv.dropContents(pipe.getWorld(), pipe.getPos());
	}
}
