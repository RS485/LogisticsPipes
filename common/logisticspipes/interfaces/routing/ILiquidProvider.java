package logisticspipes.interfaces.routing;

import java.util.Map;

import logisticspipes.request.LiquidRequest;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LiquidLogisticsPromise;
import logisticspipes.utils.LiquidIdentifier;

public interface ILiquidProvider {
	public Map<LiquidIdentifier, Integer> getAvailableLiquids();
	public void canProvide(LiquidRequest request);
	public void fullFill(LiquidLogisticsPromise promise, IRequestLiquid destination);
	public IRouter getRouter();
	
}
