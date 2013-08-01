package logisticspipes.logistics;

import java.util.List;
import java.util.TreeSet;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ILogisticsLiquidManager {
	public Pair<Integer, Integer> getBestReply(FluidStack stack, IRouter sourceRouter, List<Integer> jamList);
	public ItemStack getLiquidContainer(FluidStack stack);
	public FluidStack getLiquidFromContainer(ItemStack stack);
	public TreeSet<ItemIdentifierStack> getAvailableLiquid(List<ExitRoute> list);
}
