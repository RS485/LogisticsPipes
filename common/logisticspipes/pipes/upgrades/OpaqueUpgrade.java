package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.basic.CoreRoutedPipe;

public class OpaqueUpgrade implements IPipeUpgrade {
	
	@Override
	public boolean needsUpdate() {
		return true;
	}
	
	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return true;
	}
}
