package logisticspipes.interfaces.routing;

import logisticspipes.utils.LiquidIdentifier;

public interface IRequireReliableLiquidTransport {
	public void itemLost(LiquidIdentifier item, int amount);
	public void itemArrived(LiquidIdentifier item, int amount);
}
