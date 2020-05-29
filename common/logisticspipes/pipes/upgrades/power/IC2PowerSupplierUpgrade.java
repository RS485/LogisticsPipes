package logisticspipes.pipes.upgrades.power;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.IPipeUpgrade;

public abstract class IC2PowerSupplierUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return true;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule pipe) {
		return false;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "all" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] {};
	}

	public abstract int getPowerLevel();
}
