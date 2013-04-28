package logisticspipes.pipes.basic.liquid;

import logisticspipes.transport.PipeLiquidTransportLogistics;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.PipeTransportLiquids;

public class LogisitcsLiquidConnectionTransport extends PipeTransportLiquids {

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return super.canPipeConnect(tile, side);
	}
}
