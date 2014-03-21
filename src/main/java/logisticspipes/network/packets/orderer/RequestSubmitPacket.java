package logisticspipes.network.packets.orderer;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.RequestPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;
import net.minecraft.entity.player.EntityPlayer;

public class RequestSubmitPacket extends RequestPacket {

	public RequestSubmitPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestSubmitPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = MainProxy.proxy.getPipeInDimensionAt(getDimension(), getPosX(), getPosY(), getPosZ(), player);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		RequestHandler.request(player, getStack(), (CoreRoutedPipe) pipe.pipe);
	}
}

