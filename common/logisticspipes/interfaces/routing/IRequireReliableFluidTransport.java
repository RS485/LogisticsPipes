package logisticspipes.interfaces.routing;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;

public interface IRequireReliableFluidTransport {

	void liquidLost(FluidKey item, int amount);

	void liquidNotInserted(FluidKey item, int amount);

	void liquidArrived(FluidKey item, int amount);
}
