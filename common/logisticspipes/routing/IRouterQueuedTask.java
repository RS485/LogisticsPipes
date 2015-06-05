package logisticspipes.routing;

import logisticspipes.pipes.basic.CoreRoutedPipe;

public interface IRouterQueuedTask {

	public void call(CoreRoutedPipe pipe, IRouter router);
}
