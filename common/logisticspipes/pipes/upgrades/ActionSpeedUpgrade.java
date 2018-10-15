package logisticspipes.pipes.upgrades;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class ActionSpeedUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return false;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule module) {
		return module instanceof ModuleExtractor || module instanceof ModuleAdvancedExtractor;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] {};
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] { "extractor", "aextractor" };
	}
}
