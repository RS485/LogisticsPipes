package logisticspipes.network.packets.orderer;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;

import net.minecraft.entity.player.EntityPlayer;

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
		final LogisticsTileGenericPipe pipe = MainProxy.proxy.getPipeInDimensionAt(dimension, getPosX(), getPosY(), getPosZ(), player);
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		RequestHandler.refreshFluid(player, (CoreRoutedPipe) pipe.pipe);
	}
}
