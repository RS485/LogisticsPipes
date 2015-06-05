package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

public class LPBCPipeTransportsItems extends PipeTransportItems {

	private final LogisticsTileGenericPipe pipe;

	public LPBCPipeTransportsItems(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
	}

	@Override
	public void injectItem(TravelingItem item, ForgeDirection dir) {
		pipe.pipe.transport.injectItem(item, dir);
	}
}
