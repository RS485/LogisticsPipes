package logisticspipes.network.packets.orderer;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class RequestFluidOrdererRefreshPacket extends IntegerCoordinatesPacket {

	public RequestFluidOrdererRefreshPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestFluidOrdererRefreshPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		int dimension = getInteger();
		final TileGenericPipe pipe = MainProxy.proxy.getPipeInDimensionAt(dimension, getPosX(), getPosY(), getPosZ(), player);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		RequestHandler.refreshFluid(player, (CoreRoutedPipe) pipe.pipe);
	}
}

