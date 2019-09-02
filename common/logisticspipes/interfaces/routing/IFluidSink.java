package logisticspipes.interfaces.routing;

import logisticspipes.utils.FluidIdentifierStack;

public interface IFluidSink {

	int sinkAmount(FluidIdentifierStack stack);
}
