package logisticspipes.interfaces;

import net.minecraft.inventory.IInventory;

import net.minecraftforge.common.util.ForgeDirection;

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

	ForgeDirection getSneakyOrientation();

	boolean hasOwnSneakyUpgrade();

	IInventory getInv();

	IPipeUpgrade getUpgrade(int slot);

	DoubleCoordinates getPipePosition();
}
