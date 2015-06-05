package logisticspipes.pipes.upgrades;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class AdvancedSatelliteUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsCraftingLogistics;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule module) {
		return module instanceof ModuleCrafter;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "crafting" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] { "crafting" };
	}
}
