package logisticspipes.interfaces.routing;

import logisticspipes.utils.FluidIdentifier;

public interface IRequestFluid extends IRequest {

	void sendFailed(FluidIdentifier value1, Integer value2);
}
