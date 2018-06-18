package logisticspipes.pipes.upgrades;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.item.SimpleStackInventory;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class ModuleUpgradeManager implements ISimpleInventoryEventHandler, ISlotUpgradeManager {

	private final UpgradeManager parent;
	@Getter
	private SimpleStackInventory inv = new SimpleStackInventory(2, "UpgradeInventory", 16);
	private IPipeUpgrade[] upgrades = new IPipeUpgrade[2];
	private PipeLogisticsChassi pipe;

	private EnumFacing sneakyOrientation = null;
	private boolean isAdvancedCrafter = false;
	private boolean isFuzzyUpgrade = false;
	private int liquidCrafter = 0;
	private boolean hasByproductExtractor = false;
	private boolean hasPatternUpgrade = false;
	private int craftingCleanup = 0;

	public ModuleUpgradeManager(PipeLogisticsChassi pipe, UpgradeManager parent) {
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
			if (item != null) {
				needUpdate |= updateModule(i, upgrades, inv);
			} else if (item == null && upgrades[i] != null) {
				needUpdate |= removeUpgrade(i, upgrades);
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
			}
		}
		liquidCrafter = Math.min(liquidCrafter, ItemUpgrade.MAX_LIQUID_CRAFTER);
		craftingCleanup = Math.min(craftingCleanup, ItemUpgrade.MAX_CRAFTING_CLEANUP);
		if (needUpdate) {
			pipe.connectionUpdate();
			if (pipe.container != null) {
				pipe.container.sendUpdateToClient();
			}
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
		if(stackInSlot.getItem() instanceof ItemUpgrade) {
			upgrades[slot] = ((ItemUpgrade) stackInSlot.getItem()).getUpgradeForItem(stackInSlot, upgrades[slot]);
		} else {
			upgrades[slot] = null;
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
}
