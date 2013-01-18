package logisticspipes.logistics;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.routing.IRouter;
import logisticspipes.routing.SearchNode;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidStack;

public interface ILogisticsLiquidManager {
	public Pair<Integer, Integer> getBestReply(LiquidStack stack, IRouter sourceRouter, List<Integer> jamList);
	public ItemStack getLiquidContainer(LiquidStack stack);
	public LiquidStack getLiquidFromContainer(ItemStack stack);
	public LinkedList<ItemIdentifierStack> getAvailableLiquid(List<SearchNode> list);
}
