package logisticspipes.pipes.upgrades.power;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.IPipeUpgrade;

public abstract class IC2PowerSupplierUpgrade implements IPipeUpgrade {
	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return true;
	}
	
	public abstract int getPowerLevel();
}
