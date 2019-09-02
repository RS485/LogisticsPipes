package logisticspipes.routing;

import logisticspipes.pipes.basic.CoreRoutedPipe;

public interface IRouterQueuedTask {

	void call(CoreRoutedPipe pipe, IRouter router);
}
