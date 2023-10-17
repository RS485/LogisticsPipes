package logisticspipes.interfaces.routing;

import javax.annotation.Nonnull;

import logisticspipes.routing.IRouter;

public interface IRequest {

	@Nonnull
	IRouter getRouter();

	int getID();
}
