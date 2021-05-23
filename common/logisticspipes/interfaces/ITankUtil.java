package logisticspipes.interfaces;

import java.util.stream.Stream;

import net.minecraftforge.fluids.capability.IFluidTankProperties;

import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;

public interface ITankUtil {

	boolean containsTanks();

	int fill(FluidIdentifierStack stack, boolean doFill);

	FluidIdentifierStack drain(FluidIdentifierStack stack, boolean doDrain);

	FluidIdentifierStack drain(int amount, boolean doDrain);

	Stream<IFluidTankProperties> tanks();

	/**
	 * Type only amount is ignored
	 */
	boolean canDrain(FluidIdentifier fluid);

	int getFreeSpaceInsideTank(FluidIdentifier type);
}
