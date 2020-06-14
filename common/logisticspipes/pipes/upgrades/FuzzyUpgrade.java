package logisticspipes.pipes.upgrades;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class FuzzyUpgrade implements IPipeUpgrade {

	public static String getName() {
		return "fuzzy";
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsCraftingLogistics || pipe instanceof PipeItemsBasicLogistics;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule pipe) {
		return pipe instanceof ModuleCrafter || pipe instanceof ModuleItemSink;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "crafting", "basic" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] { "crafting", "itemsink" };
	}
}
