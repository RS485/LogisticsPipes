package logisticspipes.pipes.upgrades;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.item.SimpleStackInventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;

public class ModuleUpgradeManager implements ISimpleInventoryEventHandler, ISlotUpgradeManager {

	@Getter
	private SimpleStackInventory inv = new SimpleStackInventory(2, "UpgradeInventory", 16);

	private IPipeUpgrade[] upgrades = new IPipeUpgrade[2];
	private PipeLogisticsChassi pipe;

	private final UpgradeManager parent;

	private ForgeDirection sneakyOrientation = ForgeDirection.UNKNOWN;
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
		return hasPatternUpgrade ? true : parent.hasPatternUpgrade();
	}

	@Override
	public boolean isAdvancedSatelliteCrafter() {
		return isAdvancedCrafter ? true : parent.isAdvancedSatelliteCrafter();
	}

	@Override
	public boolean hasByproductExtractor() {
		return hasByproductExtractor ? true : parent.hasByproductExtractor();
	}

	@Override
	public int getFluidCrafter() {
		return Math.min(liquidCrafter + parent.getFluidCrafter(), ItemUpgrade.MAX_LIQUID_CRAFTER);
	}

	@Override
	public boolean isFuzzyUpgrade() {
		return isFuzzyUpgrade ? true : parent.isFuzzyUpgrade();
	}

	@Override
	public int getCrafterCleanup() {
		return Math.min(craftingCleanup + parent.getCrafterCleanup(), ItemUpgrade.MAX_CRAFTING_CLEANUP);
	}

	@Override
	public boolean hasSneakyUpgrade() {
		if (sneakyOrientation != ForgeDirection.UNKNOWN) {
			return true;
		}
		return parent.hasSneakyUpgrade();
	}

	@Override
	public ForgeDirection getSneakyOrientation() {
		if (sneakyOrientation != ForgeDirection.UNKNOWN) {
			return sneakyOrientation;
		}
		return parent.getSneakyOrientation();
	}

	@Override
	public boolean hasOwnSneakyUpgrade() {
		return sneakyOrientation != ForgeDirection.UNKNOWN;
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
		isAdvancedCrafter = false;
		isFuzzyUpgrade = false;
		liquidCrafter = 0;
		hasByproductExtractor = false;
		hasPatternUpgrade = false;
		craftingCleanup = 0;
		for (int i = 0; i < upgrades.length; i++) {
			IPipeUpgrade upgrade = upgrades[i];
			if (upgrade instanceof SneakyUpgrade && sneakyOrientation == ForgeDirection.UNKNOWN) {
				sneakyOrientation = ((SneakyUpgrade) upgrade).getSneakyOrientation();
			} else if (upgrade instanceof AdvancedSatelliteUpgrade) {
				isAdvancedCrafter = true;
			} else if (upgrade instanceof FuzzyUpgrade) {
				isFuzzyUpgrade = true;
			} else if (upgrade instanceof FluidCraftingUpgrade) {
				liquidCrafter += inv.getStackInSlot(i).stackSize;
			} else if (upgrade instanceof CraftingByproductUpgrade) {
				hasByproductExtractor = true;
			} else if (upgrade instanceof PatternUpgrade) {
				hasPatternUpgrade = true;
			} else if (upgrade instanceof CraftingCleanupUpgrade) {
				craftingCleanup += inv.getStackInSlot(i).stackSize;
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
}
