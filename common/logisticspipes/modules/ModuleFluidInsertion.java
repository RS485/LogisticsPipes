package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.FluidSinkReply;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.property.IntListProperty;
import network.rs485.logisticspipes.property.Property;

public class ModuleFluidInsertion extends LogisticsModule {

	protected final FluidRoutedPipe fluidPipe = (FluidRoutedPipe) _service;
	public final IntListProperty nextSendMax = new IntListProperty("nextSendMax");
	public final IntListProperty nextSendMin = new IntListProperty("nextSendMin");

	@NotNull
	@Override
	public String getLPName() {
		throw new RuntimeException("Cannot get LP name for " + this);
	}

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return ImmutableList.<Property<?>>builder()
			.add(nextSendMax)
			.add(nextSendMin)
			.build();
	}

	@Override
	public void tick() {
		PipeFluidTransportLogistics transport = (PipeFluidTransportLogistics) fluidPipe.transport;
		for (EnumFacing dir : EnumFacing.VALUES) {
			FluidStack stack = transport.sideTanks[dir.ordinal()].getFluid();
			if (stack == null) {
				continue;
			}
			stack = stack.copy();

			if (this.nextSendMax.get(dir.ordinal()) > 0 && stack.amount < transport.sideTanks[dir.ordinal()].getCapacity()) {
				this.nextSendMax.increase(dir.ordinal(), -1);
				continue;
			}
			if (nextSendMin.get(dir.ordinal()) > 0) {
				this.nextSendMin.increase(dir.ordinal(), -1);
				continue;
			}

			Pair<Integer, FluidSinkReply> result = SimpleServiceLocator.logisticsFluidManager.getBestReply(FluidIdentifierStack.getFromStack(stack), fluidPipe.getRouter(), new ArrayList<>());
			if (result == null || result.getValue2().sinkAmount <= 0) {
				this.nextSendMax.set(dir.ordinal(), 100);
				this.nextSendMin.set(dir.ordinal(), 10);
				continue;
			}

			if (!fluidPipe.useEnergy((int) (0.01 * result.getValue2().getSinkAmountInt()))) {
				this.nextSendMax.set(dir.ordinal(), 100);
				this.nextSendMin.set(dir.ordinal(), 10);
				continue;
			}

			FluidStack toSend = transport.sideTanks[dir.ordinal()].drain(result.getValue2().getSinkAmountInt(), true);
			ItemIdentifierStack liquidContainer = SimpleServiceLocator.logisticsFluidManager.getFluidContainer(FluidIdentifierStack.getFromStack(toSend));
			IRoutedItem routed = SimpleServiceLocator.routedItemHelper.createNewTravelItem(liquidContainer);
			routed.setDestination(result.getValue1());
			routed.setTransportMode(IRoutedItem.TransportMode.Passive);
			fluidPipe.queueRoutedItem(routed, dir);
			this.nextSendMax.set(dir.ordinal(), 100);
			this.nextSendMin.set(dir.ordinal(), 5);
		}
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean receivePassive() {
		return false;
	}
}
