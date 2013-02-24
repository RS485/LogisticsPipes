package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class SplitCraftingSatelliteUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}
	
	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsSatelliteLogistics;
	}
}
