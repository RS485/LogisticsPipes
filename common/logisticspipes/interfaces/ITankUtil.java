package logisticspipes.interfaces;

import java.util.function.Consumer;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface ITankUtil {

	boolean containsTanks();

	int fill(FluidStack stack, boolean doFill);

	FluidStack drain(int amount, boolean doDrain);

	void forEachTank(Consumer<FluidStack> fluidStackConsumer);

	boolean canDrain(Fluid fluid);
}
