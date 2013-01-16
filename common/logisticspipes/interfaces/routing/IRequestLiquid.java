package logisticspipes.interfaces.routing;

import logisticspipes.routing.IRouter;
import logisticspipes.utils.LiquidIdentifier;

public interface IRequestLiquid {
	IRouter getRouter();
	void sendFailed(LiquidIdentifier value1, Integer value2);
}
