package logisticspipes.utils;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.utils.item.ItemIdentifierStack;

public class FluidIdentifierStack implements Comparable<FluidIdentifierStack> {

	private Object ccType;
	private final FluidIdentifier _fluid;
	private int milliBuckets;

	public FluidIdentifierStack(FluidIdentifier fluid, int milliBuckets) {
		_fluid = fluid;
		setAmount(milliBuckets);
	}

	public static FluidIdentifierStack getFromStack(FluidStack stack) {
		FluidIdentifier fluid = FluidIdentifier.get(stack);
		if (fluid == null) return null;
		return new FluidIdentifierStack(fluid, stack.amount);
	}

	public static FluidIdentifierStack getFromStack(ItemIdentifierStack stack) {
		FluidIdentifier fluid = FluidIdentifier.get(stack);
		if (fluid == null) return null;
		return new FluidIdentifierStack(fluid, stack.getStackSize());
	}

	public FluidIdentifier getFluid() {
		return _fluid;
	}

	/**
	 * @return the stackSize
	 */
	public int getAmount() {
		return milliBuckets;
	}

	public void setAmount(int milliBuckets) {
		this.milliBuckets = milliBuckets;
	}

	public void lowerAmount(int milliBuckets) {
		this.milliBuckets -= milliBuckets;
	}

	public void raiseAmount(int milliBuckets) {
		this.milliBuckets += milliBuckets;
	}

	public FluidStack makeFluidStack() {
		return _fluid.makeFluidStack(milliBuckets);
	}

	@Override
	public int compareTo(FluidIdentifierStack o) {
		int c = _fluid.compareTo(o._fluid);
		if (c == 0) {
			return Integer.compare(getAmount(), o.getAmount());
		}
		return c;
	}
}
