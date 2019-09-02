package logisticspipes.utils;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.utils.item.ItemIdentifierStack;

public class FluidIdentifierStack implements Comparable<FluidIdentifierStack> {

	private Object ccType;
	private final FluidIdentifier _fluid;
	private int amount;

	public FluidIdentifierStack(FluidIdentifier fluid, int stackSize) {
		_fluid = fluid;
		setAmount(stackSize);
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
		return amount;
	}

	/**
	 * @param stackSize the stackSize to set
	 */
	public void setAmount(int stackSize) {
		this.amount = stackSize;
	}

	public void lowerAmount(int stackSize) {
		this.amount -= stackSize;
	}

	public void raiseAmount(int stackSize) {
		this.amount += stackSize;
	}

	public FluidStack makeFluidStack() {
		return _fluid.makeFluidStack(amount);
	}

	@Override
	public int compareTo(FluidIdentifierStack o) {
		int c = _fluid.compareTo(o._fluid);
		if (c == 0) {
			return getAmount() - o.getAmount();
		}
		return c;
	}
}
