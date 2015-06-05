package logisticspipes.logistics;

import java.util.List;
import java.util.TreeSet;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

import net.minecraftforge.fluids.FluidStack;

public interface ILogisticsFluidManager {

	public Pair<Integer, Integer> getBestReply(FluidStack stack, IRouter sourceRouter, List<Integer> jamList);

	public ItemIdentifierStack getFluidContainer(FluidStack stack);

	public FluidStack getFluidFromContainer(ItemIdentifierStack stack);

	public TreeSet<ItemIdentifierStack> getAvailableFluid(List<ExitRoute> list);
}
