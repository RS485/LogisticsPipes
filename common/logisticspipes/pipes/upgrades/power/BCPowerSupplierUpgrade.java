package logisticspipes.pipes.upgrades.power;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.IPipeUpgrade;

public class BCPowerSupplierUpgrade implements IPipeUpgrade {
	@Override
	public boolean needsUpdate() {
		return false;
	}
	
	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return true;
	}
}
