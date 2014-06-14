package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;


public class CraftingCleanupUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsCraftingLogistics;
	}
}
