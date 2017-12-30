package logisticspipes.logistics;

import java.util.List;
import java.util.TreeSet;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

import net.minecraftforge.fluids.FluidStack;

public interface ILogisticsFluidManager {

	public Pair<Integer, Integer> getBestReply(FluidIdentifierStack stack, IRouter sourceRouter, List<Integer> jamList);

	public ItemIdentifierStack getFluidContainer(FluidIdentifierStack stack);

	public FluidIdentifierStack getFluidFromContainer(ItemIdentifierStack stack);

	public TreeSet<FluidIdentifierStack> getAvailableFluid(List<ExitRoute> list);
}
