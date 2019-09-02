package logisticspipes.interfaces;

import java.util.function.Consumer;

import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;

public interface ITankUtil {

	boolean containsTanks();

	int fill(FluidIdentifierStack stack, boolean doFill);

	FluidIdentifierStack drain(FluidIdentifierStack stack, boolean doDrain);

	FluidIdentifierStack drain(int amount, boolean doDrain);

	void forEachFluid(Consumer<FluidIdentifierStack> fluidStackConsumer);

	/**
	 * Type only amount is ignored
	 */
	boolean canDrain(FluidIdentifier fluid);

	int getFreeSpaceInsideTank(FluidIdentifier type);
}
