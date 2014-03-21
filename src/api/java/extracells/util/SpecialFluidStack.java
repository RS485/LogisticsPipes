package extracells.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SpecialFluidStack
{
	public long amount;
	int fluidID;

	public SpecialFluidStack(Fluid fluid, long amount)
	{
		fluidID = fluid.getID();
		this.amount = amount;
	}

	public SpecialFluidStack(int id, long amount)
	{
		this.fluidID = id;
		this.amount = amount;
	}

	public long getAmount()
	{
		return amount;
	}

	public int getID()
	{
		return fluidID;
	}

	public FluidStack getFluidStack()
	{
		return new FluidStack(fluidID, (int) amount);
	}
}
