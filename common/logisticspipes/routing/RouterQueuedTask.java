package logisticspipes.routing;

import logisticspipes.pipes.basic.CoreRoutedPipe;

public interface RouterQueuedTask {

	void call(CoreRoutedPipe pipe, Router router);

}
