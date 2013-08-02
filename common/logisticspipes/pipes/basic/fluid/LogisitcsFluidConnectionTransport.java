package logisticspipes.pipes.basic.fluid;

import logisticspipes.transport.PipeFluidTransportLogistics;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.core.IMachine;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;

public class LogisitcsFluidConnectionTransport extends PipeTransportFluids {
	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportFluids || pipe2.transport instanceof PipeFluidTransportLogistics))
				return false;
		}

		if (tile instanceof IFluidHandler) {
			IFluidHandler liq = (IFluidHandler) tile;

			if (liq.getTankInfo(side) != null && liq.getTankInfo(side).length > 0)
				return true;
		}

		return tile instanceof TileGenericPipe || (tile instanceof IMachine && ((IMachine) tile).manageFluids());
	}
}
