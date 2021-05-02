package logisticspipes.interfaces.routing;

import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.FluidSinkReply;

public interface IFluidSink {

	FluidSinkReply sinkAmount(FluidIdentifierStack stack, int bestPriority);
}
