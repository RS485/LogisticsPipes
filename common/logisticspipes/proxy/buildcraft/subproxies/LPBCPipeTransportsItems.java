package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.util.EnumFacing;

import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

public class LPBCPipeTransportsItems extends PipeTransportItems {

	private final LogisticsTileGenericPipe pipe;

	public LPBCPipeTransportsItems(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
	}

	@Override
	public void injectItem(TravelingItem item, EnumFacing dir) {
		pipe.pipe.transport.injectItem(item, dir);
	}
}
