package logisticspipes.pipes.upgrades;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class CraftingMonitoringUpgrade implements IPipeUpgrade {
	
	@Override
	public boolean needsUpdate() {
		return false;
	}
	
	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return pipe instanceof PipeBlockRequestTable;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule pipe) {
		return false;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[]{"requestblock"};
	}

	@Override
	public String[] getAllowedModules() {
		return new String[]{};
	}
}
