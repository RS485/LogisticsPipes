package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.basic.CoreRoutedPipe;

public interface IPipeUpgrade {
	boolean needsUpdate();
	boolean isAllowed(CoreRoutedPipe pipe);
}
