package logisticspipes.interfaces;

import net.minecraft.util.math.Direction;

public interface IPipeUpgradeManager {

	boolean hasPowerPassUpgrade();

	boolean hasRFPowerSupplierUpgrade();

	boolean hasBCPowerSupplierUpgrade();

	int getIC2PowerLevel();

	int getSpeedUpgradeCount();

	boolean isSideDisconnected(Direction side);

	boolean hasCCRemoteControlUpgrade();

	boolean hasCraftingMonitoringUpgrade();

	boolean isOpaque();

	boolean hasUpgradeModuleUpgrade();

	boolean hasCombinedSneakyUpgrade();

	Direction[] getCombinedSneakyOrientation();

}
