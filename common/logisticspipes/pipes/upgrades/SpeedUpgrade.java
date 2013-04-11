package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.basic.CoreRoutedPipe;

public class SpeedUpgrade implements IPipeUpgrade {

	@Override
	public boolean needsUpdate() {
		return false;
	}
	
	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return true;
	}
}
