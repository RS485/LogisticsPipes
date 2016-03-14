package logisticspipes.proxy.enderio;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.PipeFluidTransportLogistics;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Created by David on 03.03.2016.
 */
public class LogisticsInsertionFluidHandler implements IFluidHandler {
	private final LogisticsTileGenericPipe pipe;

	public LogisticsInsertionFluidHandler(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
		return ((PipeFluidTransportLogistics)pipe.pipe.transport).fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {
		return ((PipeFluidTransportLogistics)pipe.pipe.transport).canFill(from, fluid);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {
		return new FluidTankInfo[0];
	}
}
