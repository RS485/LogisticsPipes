package logisticspipes.logistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.LPItems;
import logisticspipes.interfaces.routing.IFluidSink;
import logisticspipes.interfaces.routing.IProvideFluids;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.FluidSinkReply;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

public class LogisticsFluidManager implements ILogisticsFluidManager {

	@Override
	public @Nullable Pair<Integer, FluidSinkReply> getBestReply(FluidIdentifierStack stack, IRouter sourceRouter, List<Integer> jamList) {
		Optional<Pair<Integer, FluidSinkReply>> bestReply = sourceRouter.getIRoutersByCost().stream()
				.filter(it -> it.containsFlag(PipeRoutingConnectionType.canRouteTo) &&
						it.destination.getId() != sourceRouter.getId() &&
						!jamList.contains(it.destination.getSimpleID()) &&
						it.destination.getPipe() instanceof IFluidSink &&
						it.destination.getPipe().isEnabled() &&
						!it.destination.getPipe().isOnSameContainer(sourceRouter.getPipe()))
				.sorted()
				.map(it -> new Pair<>(it.destination.getSimpleID(),
						((IFluidSink) it.destination.getPipe()).sinkAmount(stack)))
				.filter(pair -> pair.getValue2() != null && pair.getValue2().sinkAmount != 0L)
				.reduce((left, right) ->
						left.getValue2().fixedPriority.compareTo(right.getValue2().fixedPriority) < 0 ? right : left);
		bestReply.ifPresent(pair -> {
			ServerRouter serverRouter = SimpleServiceLocator.routerManager.getServerRouter(pair.getValue1());
			if (serverRouter == null) return;
			CoreRoutedPipe pipe = serverRouter.getPipe();
			if (pipe != null) pipe.spawnParticle(Particles.BlueParticle, 10);
		});
		return bestReply.orElse(null);
	}

	@Override
	public ItemIdentifierStack getFluidContainer(FluidIdentifierStack stack) {
		ItemStack item = new ItemStack(LPItems.fluidContainer, 1);
		NBTTagCompound nbt = new NBTTagCompound();
		stack.makeFluidStack().writeToNBT(nbt);
		item.setTagCompound(nbt);
		return ItemIdentifierStack.getFromStack(item);
	}

	@Override
	public FluidIdentifierStack getFluidFromContainer(ItemIdentifierStack stack) {
		ItemStack itemStack = stack.makeNormalStack();
		if (itemStack.getItem() instanceof LogisticsFluidContainer && stack.getItem().tag != null) {
			return FluidIdentifierStack.getFromStack(FluidStack.loadFluidStackFromNBT(stack.getItem().tag));
		}
		return null;
	}

	@Override
	public TreeSet<FluidIdentifierStack> getAvailableFluid(List<ExitRoute> validDestinations) {
		Map<FluidIdentifier, Integer> allAvailableItems = new HashMap<>();
		for (ExitRoute r : validDestinations) {
			if (r == null) {
				continue;
			}
			if (!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) {
				continue;
			}
			if (!(r.destination.getPipe() instanceof IProvideFluids)) {
				continue;
			}

			IProvideFluids provider = (IProvideFluids) r.destination.getPipe();
			Map<FluidIdentifier, Integer> allItems = provider.getAvailableFluids();

			for (Entry<FluidIdentifier, Integer> liquid : allItems.entrySet()) {
				Integer amount = allAvailableItems.get(liquid.getKey());
				if (amount == null) {
					allAvailableItems.put(liquid.getKey(), liquid.getValue());
				} else {
					long addition = ((long) amount) + liquid.getValue();
					if (addition > Integer.MAX_VALUE) {
						addition = Integer.MAX_VALUE;
					}
					allAvailableItems.put(liquid.getKey(), (int) addition);
				}
			}
		}
		return allAvailableItems.entrySet().stream()
				.map(item -> new FluidIdentifierStack(item.getKey(), item.getValue()))
				.collect(Collectors.toCollection(TreeSet::new));
	}
}
