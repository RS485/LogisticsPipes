package logisticspipes.interfaces.routing;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.utils.FluidIdentifierStack;

public interface IFluidSink {

	public int sinkAmount(FluidIdentifierStack stack);
}
