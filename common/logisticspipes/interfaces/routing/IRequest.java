package logisticspipes.interfaces.routing;

import logisticspipes.routing.IRouter;

public interface IRequest {

	public IRouter getRouter();

	public int getID();
}
