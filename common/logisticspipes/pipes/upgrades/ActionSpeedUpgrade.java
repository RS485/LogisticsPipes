package logisticspipes.pipes.upgrades;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;
import network.rs485.logisticspipes.module.AsyncExtractorModule;

public class ActionSpeedUpgrade implements IPipeUpgrade {

	public static String getName() {
		return "action_speed";
	}

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
		return module instanceof AsyncExtractorModule || module instanceof AsyncAdvancedExtractor;
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
