package logisticspipes.pipes.upgrades;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class AdvancedSatelliteUpgrade implements IPipeUpgrade {

	public static String getName() {
		return "satellite_advanced";
	}

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
