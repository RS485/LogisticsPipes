package logisticspipes.pipes.upgrades;

import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class PatternUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsSupplierLogistics;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule pipe) {
		return pipe instanceof ModuleActiveSupplier;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[]{"supplier"};
	}

	@Override
	public String[] getAllowedModules() {
		return new String[]{};
	}
}
