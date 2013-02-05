package logisticspipes.logistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.ILiquidSink;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.SearchNode;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.liquids.LiquidStack;

public class LogisticsLiquidManager implements ILogisticsLiquidManager {
	
	public Pair<Integer, Integer> getBestReply(LiquidStack stack, IRouter sourceRouter, List<Integer> jamList) {
		for (SearchNode candidateRouter : sourceRouter.getIRoutersByCost()){
			if(!candidateRouter.containsFlag(PipeRoutingConnectionType.canRouteTo)) continue;
			if(candidateRouter.node.getSimpleID() == sourceRouter.getSimpleID()) continue;
			if(jamList.contains(candidateRouter.node.getSimpleID())) continue;
			
			if (candidateRouter.node.getPipe() == null || !candidateRouter.node.getPipe().isEnabled()) continue;
			CoreRoutedPipe pipe = candidateRouter.node.getPipe();
			
			if(!(pipe instanceof ILiquidSink)) continue;
			
			int amount = ((ILiquidSink)pipe).sinkAmount(stack);
			if(amount > 0) {
				Pair<Integer, Integer> result = new Pair<Integer, Integer>(candidateRouter.node.getSimpleID(), amount);
				return result;
			}
		}
		Pair<Integer, Integer> result = new Pair<Integer, Integer>(null, 0);
		return result;
	}

	@Override
	public ItemStack getLiquidContainer(LiquidStack stack) {
		ItemStack item = new ItemStack(LogisticsPipes.LogisticsLiquidContainer, 1);
		NBTTagCompound nbt = new NBTTagCompound("tag");
		stack.writeToNBT(nbt);
		item.setTagCompound(nbt);
		return item;
	}

	@Override
	public LiquidStack getLiquidFromContainer(ItemStack stack) {
		if(stack.getItem() instanceof LogisticsLiquidContainer && stack.hasTagCompound()) {
			return LiquidStack.loadLiquidStackFromNBT(stack.getTagCompound());
		}
		return null;
	}
	
	@Override
	public TreeSet<ItemIdentifierStack> getAvailableLiquid(List<SearchNode> validDestinations) {
		Map<ItemIdentifier, Integer> allAvailableItems = new HashMap<ItemIdentifier, Integer>();
		for(SearchNode r: validDestinations){
			if(r == null) continue;
			if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if (!(r.node.getPipe() instanceof ILiquidProvider)) continue;

			ILiquidProvider provider = (ILiquidProvider) r.node.getPipe();
			Map<LiquidIdentifier, Integer> allItems = provider.getAvailableLiquids();
			
			for (Entry<LiquidIdentifier, Integer> liquid : allItems.entrySet()){
				Integer amount = allAvailableItems.get(liquid.getKey().getItemIdentifier());
				if (amount==null){
					allAvailableItems.put(liquid.getKey().getItemIdentifier(), liquid.getValue());
				} else {
					allAvailableItems.put(liquid.getKey().getItemIdentifier(), amount + liquid.getValue());
				}
			}
		}
		TreeSet<ItemIdentifierStack> itemIdentifierStackList = new TreeSet<ItemIdentifierStack>();
		for(ItemIdentifier item:allAvailableItems.keySet()) {
			itemIdentifierStackList.add(new ItemIdentifierStack(item, allAvailableItems.get(item)));
		}
		return itemIdentifierStackList;
	}
}
