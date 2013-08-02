package logisticspipes.interfaces.routing;

import java.util.Map;

import logisticspipes.request.FluidRequestTreeNode;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.utils.FluidIdentifier;

public interface IFluidProvider {
	public Map<FluidIdentifier, Integer> getAvailableFluids();
	public void canProvide(FluidRequestTreeNode request, int donePrommises);
	public void fullFill(FluidLogisticsPromise promise, IRequestFluid destination);
	public IRouter getRouter();
	
}
