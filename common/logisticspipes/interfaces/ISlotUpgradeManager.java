package logisticspipes.interfaces;

import net.minecraft.util.EnumFacing;

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
}
