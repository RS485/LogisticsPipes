package extracells.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidInterface {
	
	public void setFilter(EnumFacing side, Fluid fluid);
	
	public Fluid getFilter(EnumFacing side);
	
	public IFluidTank getFluidTank(EnumFacing side);
	
	public void setFluidTank(EnumFacing side, FluidStack fluid);
	
	public IInventory getPatternInventory();
	
}
