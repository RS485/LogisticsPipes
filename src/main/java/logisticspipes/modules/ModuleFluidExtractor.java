package logisticspipes.modules;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.interfaces.ITankUtil;
import logisticspipes.pipes.PipeFluidUtil;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.FluidIdentifierStack;
import network.rs485.logisticspipes.property.IntListProperty;
import network.rs485.logisticspipes.property.Property;

public class ModuleFluidExtractor extends ModuleFluidInsertion {

	private static final int flowRate = 500;
	private static final int energyPerFlow = 5;

	public final IntListProperty liquidToExtract = new IntListProperty("liquidToExtract");

	@NotNull
	@Override
	public String getLPName() {
		throw new RuntimeException("Cannot get LP name for " + this);
	}

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.singletonList(liquidToExtract);
	}

	@Override
	public void tick() {
		PipeFluidUtil.INSTANCE.getAdjacentTanks(fluidPipe, false)
			.forEach(tankData -> extractFrom(tankData.getValue2(), tankData.getValue1().getDirection()));
	}

	private void extractFrom(ITankUtil container, EnumFacing side) {
		int sideID = side.ordinal();
		FluidStack contained = ((PipeFluidTransportLogistics) fluidPipe.transport).getTankProperties(side)[0].getContents();
		int amountMissing = ((PipeFluidTransportLogistics) fluidPipe.transport).getSideCapacity() - (contained != null ? contained.amount : 0);
		if (liquidToExtract.get(sideID) < Math.min(ModuleFluidExtractor.flowRate, amountMissing)) {
			if (fluidPipe.useEnergy(ModuleFluidExtractor.energyPerFlow)) {
				liquidToExtract.set(sideID, Math.min(ModuleFluidExtractor.flowRate, amountMissing));
			}
		}
		FluidIdentifierStack extracted = container.drain(Math.min(liquidToExtract.get(sideID), ModuleFluidExtractor.flowRate), false);

		int inserted = 0;
		if (extracted != null) {
			inserted = ((PipeFluidTransportLogistics) fluidPipe.transport).fill(side, extracted.makeFluidStack(), true);
			container.drain(inserted, true);
		}
		liquidToExtract.increase(sideID, -inserted);
	}
}
