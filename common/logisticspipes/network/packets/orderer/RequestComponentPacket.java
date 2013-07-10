package logisticspipes.network.packets.orderer;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.RequestPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class RequestComponentPacket extends RequestPacket {

	public RequestComponentPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestComponentPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = MainProxy.proxy.getPipeInDimensionAt(getDimension(), getPosX(), getPosY(), getPosZ(), player);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		RequestHandler.simulate(player, getStack(), (CoreRoutedPipe) pipe.pipe);
	}
}

