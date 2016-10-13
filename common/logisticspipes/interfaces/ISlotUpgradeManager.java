package logisticspipes.interfaces;

import net.minecraft.inventory.IInventory;

import logisticspipes.pipes.upgrades.IPipeUpgrade;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface ISlotUpgradeManager {

	boolean hasPatternUpgrade();

	boolean isAdvancedSatelliteCrafter();

	boolean hasByproductExtractor();

	int getFluidCrafter();

	boolean isFuzzyUpgrade();

	int getCrafterCleanup();

	boolean hasSneakyUpgrade();

	EnumFacing getSneakyOrientation();

	boolean hasOwnSneakyUpgrade();

	IInventory getInv();

	IPipeUpgrade getUpgrade(int slot);

	DoubleCoordinates getPipePosition();
}
