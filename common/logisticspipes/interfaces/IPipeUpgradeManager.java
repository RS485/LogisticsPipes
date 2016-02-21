package logisticspipes.interfaces;

import net.minecraft.util.EnumFacing;

public interface IPipeUpgradeManager {

	boolean hasPowerPassUpgrade();

	boolean hasRFPowerSupplierUpgrade();

	int getIC2PowerLevel();

	int getSpeedUpgradeCount();

	boolean isSideDisconnected(EnumFacing side);

	boolean hasCCRemoteControlUpgrade();

	boolean hasCraftingMonitoringUpgrade();

	boolean isOpaque();

	boolean hasUpgradeModuleUpgrade();

	boolean hasCombinedSneakyUpgrade();

	EnumFacing[] getCombinedSneakyOrientation();

}
