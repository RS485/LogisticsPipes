package logisticspipes.interfaces.routing;

import logisticspipes.utils.FluidIdentifier;

public interface IRequireReliableFluidTransport {

	void liquidLost(FluidIdentifier item, int amount);

	void liquidNotInserted(FluidIdentifier item, int amount);

	void liquidArrived(FluidIdentifier item, int amount);
}
