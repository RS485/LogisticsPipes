package logisticspipes.logistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import logisticspipes.interfaces.routing.FluidRequestProvider;
import logisticspipes.interfaces.routing.IFluidSink;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.Router;
import network.rs485.logisticspipes.item.FluidContainerItem;
import network.rs485.logisticspipes.util.FluidReply;

public class LogisticsFluidManagerImpl implements LogisticsFluidManager {

	public static final LogisticsFluidManagerImpl INSTANCE = new LogisticsFluidManagerImpl();

	@Override
	public FluidReply getBestReply(FluidVolume stack, Router sourceRouter, List<Integer> jamList) {
		for (ExitRoute candidateRouter : sourceRouter.getIRoutersByCost()) {
			if (!candidateRouter.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
				continue;
			}
			if (candidateRouter.destination.getId().equals(sourceRouter.getId())) {
				continue;
			}
			if (jamList.contains(candidateRouter.destination.getSimpleId())) {
				continue;
			}

			if (candidateRouter.destination.getPipe() == null || !candidateRouter.destination.getPipe().isEnabled()) {
				continue;
			}
			CoreRoutedPipe pipe = candidateRouter.destination.getPipe();

			if (!(pipe instanceof IFluidSink)) {
				continue;
			}

			int amount = ((IFluidSink) pipe).sinkAmount(stack);
			if (amount > 0) {
				return new FluidReply(candidateRouter.destination.getId(), amount);
			}
		}
		return null;
	}

	@Override
	public ItemStack getFluidContainer(FluidVolume stack) {
		return FluidContainerItem.Companion.makeStack(stack);
	}

	@Override
	public FluidVolume getFluidFromContainer(ItemStack stack) {
		return FluidContainerItem.Companion.getFluid(stack);
	}

	@Override
	public TreeSet<FluidVolume> getAvailableFluid(List<ExitRoute> validDestinations) {
		Map<FluidKey, Integer> allAvailableItems = new HashMap<>();
		for (ExitRoute r : validDestinations) {
			if (r == null) continue;
			if (!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if (!(r.destination.getPipe() instanceof FluidRequestProvider)) continue;

			FluidRequestProvider provider = (FluidRequestProvider) r.destination.getPipe();
			Map<FluidKey, Integer> allItems = provider.getAvailableFluids();

			for (Entry<FluidKey, Integer> liquid : allItems.entrySet()) {
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
				.map(item -> FluidVolume.create(item.getKey(), item.getValue()))
				.collect(Collectors.toCollection(TreeSet::new));
	}
}
