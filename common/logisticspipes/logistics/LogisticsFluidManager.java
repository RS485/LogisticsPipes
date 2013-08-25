package logisticspipes.logistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFluidProvider;
import logisticspipes.interfaces.routing.IFluidSink;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class LogisticsFluidManager implements ILogisticsFluidManager {
	
	@Override
	public Pair<Integer, Integer> getBestReply(FluidStack stack, IRouter sourceRouter, List<Integer> jamList) {
		for (ExitRoute candidateRouter : sourceRouter.getIRoutersByCost()){
			if(!candidateRouter.containsFlag(PipeRoutingConnectionType.canRouteTo)) continue;
			if(candidateRouter.destination.getSimpleID() == sourceRouter.getSimpleID()) continue;
			if(jamList.contains(candidateRouter.destination.getSimpleID())) continue;
			
			if (candidateRouter.destination.getPipe() == null || !candidateRouter.destination.getPipe().isEnabled()) continue;
			CoreRoutedPipe pipe = candidateRouter.destination.getPipe();
			
			if(!(pipe instanceof IFluidSink)) continue;
			
			int amount = ((IFluidSink)pipe).sinkAmount(stack);
			if(amount > 0) {
				Pair<Integer, Integer> result = new Pair<Integer, Integer>(candidateRouter.destination.getSimpleID(), amount);
				return result;
			}
		}
		Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
		return result;
	}

	@Override
	public ItemStack getFluidContainer(FluidStack stack) {
		ItemStack item = new ItemStack(LogisticsPipes.LogisticsFluidContainer, 1);
		NBTTagCompound nbt = new NBTTagCompound("tag");
		stack.writeToNBT(nbt);
		item.setTagCompound(nbt);
		return item;
	}

	@Override
	public FluidStack getFluidFromContainer(ItemStack stack) {
		if(stack.getItem() instanceof LogisticsFluidContainer && stack.hasTagCompound()) {
			return FluidStack.loadFluidStackFromNBT(stack.getTagCompound());
		}
		return null;
	}

	@Override
	public TreeSet<ItemIdentifierStack> getAvailableFluid(List<ExitRoute> validDestinations) {
		Map<FluidIdentifier, Integer> allAvailableItems = new HashMap<FluidIdentifier, Integer>();
		for(ExitRoute r: validDestinations){
			if(r == null) continue;
			if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if (!(r.destination.getPipe() instanceof IFluidProvider)) continue;

			IFluidProvider provider = (IFluidProvider) r.destination.getPipe();
			Map<FluidIdentifier, Integer> allItems = provider.getAvailableFluids();
			
			for (Entry<FluidIdentifier, Integer> liquid : allItems.entrySet()){
				Integer amount = allAvailableItems.get(liquid.getKey());
				if (amount==null){
					allAvailableItems.put(liquid.getKey(), liquid.getValue());
				} else {
					allAvailableItems.put(liquid.getKey(), amount + liquid.getValue());
				}
			}
		}
		TreeSet<ItemIdentifierStack> itemIdentifierStackList = new TreeSet<ItemIdentifierStack>();
		for(Entry<FluidIdentifier, Integer> item:allAvailableItems.entrySet()) {
			//itemIdentifierStackList.add(new ItemIdentifierStack(item.getKey(), item.getValue()));
		}
		return itemIdentifierStackList;
	}
}
