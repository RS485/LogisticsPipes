package logisticspipes.interfaces.routing;

import javax.annotation.Nullable;

import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.FluidSinkReply;

public interface IFluidSink {

	@Nullable
	FluidSinkReply sinkAmount(FluidIdentifierStack stack);
}
