package logisticspipes.interfaces;

import net.minecraftforge.common.util.ForgeDirection;

public interface ISlotUpgradeManager {
	boolean hasPatternUpgrade();
	boolean isAdvancedSatelliteCrafter();
	boolean hasByproductExtractor();
	int getFluidCrafter();
	boolean isFuzzyCrafter();
	int getCrafterCleanup();
	boolean hasSneakyUpgrade();
	ForgeDirection getSneakyOrientation();
	boolean hasCombinedSneakyUpgrade();
	ForgeDirection[] getCombinedSneakyOrientation();
}
