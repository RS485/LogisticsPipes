package logisticspipes.transport;

import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.src.TileEntity;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.transport.TileGenericPipe;

public class CraftingPipeTransport extends PipeTransportLogistics {
	@Override
	public boolean isPipeConnected(TileEntity tile) {
		if(SimpleServiceLocator.ccProxy.isTurtle(tile)) return false;
		return super.isPipeConnected(tile);
	}
}
