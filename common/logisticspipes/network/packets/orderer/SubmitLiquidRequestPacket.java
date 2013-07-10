package logisticspipes.network.packets.orderer;

import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.RequestPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class SubmitLiquidRequestPacket extends RequestPacket {

	public SubmitLiquidRequestPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SubmitLiquidRequestPacket(getId());
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
		if (!(pipe.pipe instanceof IRequestLiquid)) {
			return;
		}
		RequestHandler.requestLiquid(player, getStack(), (CoreRoutedPipe) pipe.pipe, (IRequestLiquid) pipe.pipe);
	}
}

