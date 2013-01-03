package logisticspipes.logistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.ILiquidSink;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.liquids.LiquidStack;

public class LogisticsLiquidManager implements ILogisticsLiquidManager {
	
	public Pair<UUID, Integer> getBestReply(LiquidStack stack, IRouter sourceRouter, List<UUID> jamList) {
		for (IRouter candidateRouter : sourceRouter.getIRoutersByCost()){
			if(candidateRouter.getId().equals(sourceRouter.getId())) continue;
			if(jamList.contains(candidateRouter.getId())) continue;
			
			if (candidateRouter.getPipe() == null || !candidateRouter.getPipe().isEnabled()) continue;
			CoreRoutedPipe pipe = candidateRouter.getPipe();
			
			if(!(pipe instanceof ILiquidSink)) continue;
			
			int amount = ((ILiquidSink)pipe).sinkAmount(stack);
			if(amount > 0) {
				Pair<UUID, Integer> result = new Pair<UUID, Integer>(candidateRouter.getId(), amount);
				return result;
			}
		}
		Pair<UUID, Integer> result = new Pair<UUID, Integer>(null, 0);
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
	public LinkedList<ItemIdentifierStack> getAvailableLiquid(List<IRouter> validDestinations) {
		Map<ItemIdentifier, Integer> allAvailableItems = new HashMap<ItemIdentifier, Integer>();
		for(IRouter r: validDestinations){
			if(r == null) continue;
			if (!(r.getPipe() instanceof ILiquidProvider)) continue;

			ILiquidProvider provider = (ILiquidProvider) r.getPipe();
			Map<LiquidIdentifier, Integer> allItems = provider.getAvailableLiquids();
			
			for (LiquidIdentifier liquid : allItems.keySet()){
				if (!allAvailableItems.containsKey(liquid.getItemIdentifier())){
					allAvailableItems.put(liquid.getItemIdentifier(), allItems.get(liquid));
				} else {
					allAvailableItems.put(liquid.getItemIdentifier(), allAvailableItems.get(liquid.getItemIdentifier()) + allItems.get(liquid));
				}
			}
		}
		LinkedList<ItemIdentifierStack> itemIdentifierStackList = new LinkedList<ItemIdentifierStack>();
		for(ItemIdentifier item:allAvailableItems.keySet()) {
			itemIdentifierStackList.add(new ItemIdentifierStack(item, allAvailableItems.get(item)));
		}
		return itemIdentifierStackList;
	}
}
