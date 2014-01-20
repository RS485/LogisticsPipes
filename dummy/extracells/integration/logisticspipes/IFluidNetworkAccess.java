package extracells.integration.logisticspipes;

import extracells.util.SpecialFluidStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IFluidNetworkAccess
{
	/**
	 * Used to get a {@link List<extracells.util.SpecialFluidStack>} of all the Fluids in the ME Network
	 * 
	 * @return a List of all the Fluids in the ME Network, represented by {@link SpecialFluidStack}, normal FluidStacks, but with a long instead of an int for the amount (null if network isn't valid/doesn't have power)
	 */
	public List<SpecialFluidStack> getFluidsInNetwork();

	/**
	 * Used to drain (or simulate the draining of ) a FluidStack from the network
	 * 
	 * @param toDrain {@link FluidStack} representing the {@link net.minecraftforge.fluids.Fluid} and Amount to be drained from the ME Network
	 * @param doDrain Drain or Simulate?
	 * @return how much fluid was drained in mB
	 */
	public long drainFromNetwork(FluidStack toDrain, boolean doDrain);

	/**
	 * Used to fill (or simulate the filling of ) a FluidStack to the network
	 * 
	 * @param toFill {@link FluidStack} representing the {@link net.minecraftforge.fluids.Fluid} and Amount to be filled to the ME Network
	 * @param doFill Fill or Simulate?
	 * @return how much fluid was filled in mB
	 */
	public long fillToNetwork(FluidStack toFill, boolean doFill);

	/**
	 * Used to get the Controller of the network
	 * 
	 * @return the {@link TileEntity} of the ME Controller of the Network (null if network isn't valid/doesn't have power)
	 */
	public TileEntity getNetworkController();
}
