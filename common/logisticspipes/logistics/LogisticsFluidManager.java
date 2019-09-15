package logisticspipes.logistics;

import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.Router;
import network.rs485.logisticspipes.util.FluidReply;

public interface LogisticsFluidManager {

	FluidReply getBestReply(FluidVolume stack, Router sourceRouter, List<Integer> jamList);

	ItemStack getFluidContainer(FluidVolume stack);

	FluidVolume getFluidFromContainer(ItemStack stack);

	Set<FluidVolume> getAvailableFluid(List<ExitRoute> list);

}
