package logisticspipes.logistics;

import java.util.List;
import java.util.TreeSet;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

public interface ILogisticsFluidManager {

	Pair<Integer, Integer> getBestReply(FluidIdentifierStack stack, IRouter sourceRouter, List<Integer> jamList);

	ItemIdentifierStack getFluidContainer(FluidIdentifierStack stack);

	FluidIdentifierStack getFluidFromContainer(ItemIdentifierStack stack);

	TreeSet<FluidIdentifierStack> getAvailableFluid(List<ExitRoute> list);
}
