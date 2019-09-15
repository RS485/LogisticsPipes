package logisticspipes.interfaces.routing;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

public interface IFluidSink {
	int sinkAmount(FluidVolume stack);
}
