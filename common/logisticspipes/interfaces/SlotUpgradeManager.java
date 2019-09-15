package logisticspipes.interfaces;

import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import network.rs485.logisticspipes.pipe.upgrade.Upgrade;

public interface SlotUpgradeManager {

	boolean hasPatternUpgrade();

	boolean isAdvancedSatelliteCrafter();

	boolean hasByproductExtractor();

	int getFluidCrafter();

	boolean isFuzzyUpgrade();

	int getCrafterCleanup();

	boolean hasSneakyUpgrade();

	Direction getSneakyOrientation();

	boolean hasOwnSneakyUpgrade();

	Inventory getInv();

	Upgrade getUpgrade(int slot);

	BlockPos getPipePosition();

	int getActionSpeedUpgrade();

	int getItemExtractionUpgrade();

	int getItemStackExtractionUpgrade();

}
