package logisticspipes.interfaces.routing;

import logisticspipes.utils.FluidIdentifier;

public interface IRequireReliableFluidTransport {
	public void liquidLost(FluidIdentifier item, int amount);
	public void liquidNotInserted(FluidIdentifier item, int amount);
	public void liquidArrived(FluidIdentifier item, int amount);
}
