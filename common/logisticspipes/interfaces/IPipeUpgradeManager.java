package logisticspipes.interfaces;

import net.minecraftforge.common.util.ForgeDirection;

public interface IPipeUpgradeManager {

	boolean hasPowerPassUpgrade();

	boolean hasRFPowerSupplierUpgrade();

	int getIC2PowerLevel();

	int getSpeedUpgradeCount();

	boolean isSideDisconnected(ForgeDirection side);

	boolean hasCCRemoteControlUpgrade();

	boolean hasCraftingMonitoringUpgrade();

	boolean isOpaque();

	boolean hasUpgradeModuleUpgrade();

	boolean hasCombinedSneakyUpgrade();

	ForgeDirection[] getCombinedSneakyOrientation();

}
