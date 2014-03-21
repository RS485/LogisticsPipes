package logisticspipes.interfaces.routing;

import logisticspipes.routing.IRouter;
import logisticspipes.utils.FluidIdentifier;

public interface IRequestFluid {
	IRouter getRouter();
	void sendFailed(FluidIdentifier value1, Integer value2);
}
