package logisticspipes.interfaces.routing;

import logisticspipes.utils.LiquidIdentifier;

public interface IRequireReliableLiquidTransport {
	public void liquidLost(LiquidIdentifier item, int amount);
	public void liquidNotInserted(LiquidIdentifier item, int amount);
	public void liquidArrived(LiquidIdentifier item, int amount);
}
