package logisticspipes.pipes.upgrades;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;
import network.rs485.logisticspipes.module.AsyncExtractorModule;

public class ItemStackExtractionUpgrade implements IPipeUpgrade {

	public static String getName() {
		return "item_stack_extraction";
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsCraftingLogistics || pipe instanceof PipeItemsProviderLogistics;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule module) {
		return module instanceof ModuleCrafter || module instanceof ModuleProvider
				|| module instanceof AsyncExtractorModule || module instanceof AsyncAdvancedExtractor;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "crafting", "provider" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] { "crafting", "provider", "extractor", "aextractor" };
	}
}
