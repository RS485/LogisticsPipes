package logisticspipes.network.packets.orderer;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class OrdererRefreshRequestPacket extends IntegerCoordinatesPacket {

	public OrdererRefreshRequestPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new OrdererRefreshRequestPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		int dimension = (getInteger() - (getInteger() % 10)) / 10;
		final TileGenericPipe pipe = MainProxy.proxy.getPipeInDimensionAt(dimension, getPosX(), getPosY(), getPosZ(), player);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		RequestHandler.DisplayOptions option;
		switch(getInteger() % 10) {
			case 0:
				option = RequestHandler.DisplayOptions.Both;
				break;
			case 1:
				option = RequestHandler.DisplayOptions.SupplyOnly;
				break;
			case 2:
				option = RequestHandler.DisplayOptions.CraftOnly;
				break;
			default:
				option = RequestHandler.DisplayOptions.Both;
				break;
		}
		RequestHandler.refresh(player, (CoreRoutedPipe) pipe.pipe, option);
	}
}

