package logisticspipes.logistics;

import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.Router;
import network.rs485.logisticspipes.util.FluidReply;

public interface LogisticsFluidManager {

	static LogisticsFluidManager getInstance() {
		return LogisticsFluidManagerImpl.INSTANCE;
	}

	FluidReply getBestReply(FluidVolume stack, Router sourceRouter, List<Integer> jamList);

	@Deprecated
		// Use FluidContainerItem.makeStack() instead
	ItemStack getFluidContainer(FluidVolume stack);

	@Deprecated
		// Use FluidContainerItem.getFluid() instead
	FluidVolume getFluidFromContainer(ItemStack stack);

	Set<FluidVolume> getAvailableFluid(List<ExitRoute> list);

}
