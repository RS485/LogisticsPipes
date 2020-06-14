package logisticspipes.pipes.upgrades;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class CombinedSneakyUpgrade implements IPipeUpgrade {

	public static String getName() {
		return "sneaky_combination";
	}

	@Override
	public boolean needsUpdate() {
		return false;
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
}
