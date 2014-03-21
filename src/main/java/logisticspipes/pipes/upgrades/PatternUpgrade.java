package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class PatternUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsSupplierLogistics;
	}
}
