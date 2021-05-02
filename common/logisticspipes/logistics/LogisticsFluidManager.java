package logisticspipes.logistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidSinkReply;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.LPItems;
import logisticspipes.interfaces.routing.IFluidSink;
import logisticspipes.interfaces.routing.IProvideFluids;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

public class LogisticsFluidManager implements ILogisticsFluidManager {

	@Override
	public Pair<Integer, FluidSinkReply> getBestReply(FluidIdentifierStack stack, IRouter sourceRouter, List<Integer> jamList) {
		final Integer[] resultRouterId = { null };
		final FluidSinkReply[] result = { null };
		sourceRouter.getIRoutersByCost().stream()
				.filter(it -> it.containsFlag(PipeRoutingConnectionType.canRouteTo) &&
						it.destination.getId() != sourceRouter.getId() &&
						!jamList.contains(it.destination.getSimpleID()) &&
						it.destination.getPipe() != null &&
						it.destination.getPipe().isEnabled() &&
						!it.destination.getPipe().isOnSameContainer(sourceRouter.getPipe()) &&
						it.destination.getPipe() instanceof IFluidSink
		).sorted().forEachOrdered(it -> {
			FluidSinkReply reply;
			IFluidSink pipe = (IFluidSink) it.destination.getPipe();
			if (result[0] == null) {
				reply = pipe.sinkAmount(stack, -1);
			}
			else {
				reply = pipe.sinkAmount(stack, result[0].fixedFluidPriority.ordinal());
			}

			if (reply  != null && reply.sinkAmount != 0 && (result[0] == null || reply.fixedFluidPriority.ordinal() > result[0].fixedFluidPriority.ordinal() || reply.fixedFluidPriority.ordinal() == result[0].fixedFluidPriority.ordinal())) {
				resultRouterId[0] = it.destination.getSimpleID();
				result[0] = reply;
			}
		});

		if (result[0] != null && result[0].sinkAmount != 0) {
			if (resultRouterId[0] != null) {
				CoreRoutedPipe pipe = SimpleServiceLocator.routerManager.getServerRouter(resultRouterId[0]).getPipe();
				pipe.spawnParticle(Particles.BlueParticle, 10);
				return new Pair<>(resultRouterId[0], result[0]);
			}
		}
		return new Pair<>(0, null);
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
