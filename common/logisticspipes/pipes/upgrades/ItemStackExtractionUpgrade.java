package logisticspipes.pipes.upgrades;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class ItemStackExtractionUpgrade implements IPipeUpgrade {

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
		return module instanceof ModuleCrafter || module instanceof ModuleProvider;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "crafting", "provider" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] { "crafting", "provider" };
	}
}
