package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

public class HUDStopWatchingPacket extends IntegerCoordinatesPacket {

	public HUDStopWatchingPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new HUDStopWatchingPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (pipe.pipe instanceof IWatchingHandler) {
			IWatchingHandler handler = (IWatchingHandler) pipe.pipe;
			handler.playerStopWatching(player, getInteger());
		}
	}
}
