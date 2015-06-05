package logisticspipes.pipes.upgrades;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class UpgradeModuleUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return pipe instanceof PipeLogisticsChassi;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule pipe) {
		return false;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "chassi" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] {};
	}
}
