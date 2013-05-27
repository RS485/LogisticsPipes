package logisticspipes.interfaces.routing;

import java.util.Map;

import logisticspipes.request.LiquidRequestTreeNode;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LiquidLogisticsPromise;
import logisticspipes.utils.LiquidIdentifier;

public interface ILiquidProvider {
	public Map<LiquidIdentifier, Integer> getAvailableLiquids();
	public void canProvide(LiquidRequestTreeNode request, int donePrommises);
	public void fullFill(LiquidLogisticsPromise promise, IRequestLiquid destination);
	public IRouter getRouter();
	
}
