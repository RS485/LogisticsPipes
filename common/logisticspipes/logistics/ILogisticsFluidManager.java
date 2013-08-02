package logisticspipes.logistics;

import java.util.List;
import java.util.TreeSet;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ILogisticsFluidManager {
	public Pair<Integer, Integer> getBestReply(FluidStack stack, IRouter sourceRouter, List<Integer> jamList);
	public ItemStack getFluidContainer(FluidStack stack);
	public FluidStack getFluidFromContainer(ItemStack stack);
	public TreeSet<ItemIdentifierStack> getAvailableFluid(List<ExitRoute> list);
}
