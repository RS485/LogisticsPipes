package logisticspipes.interfaces.routing;

import logisticspipes.routing.IRouter;

public interface IRequest {

	IRouter getRouter();

	int getID();
}
