package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class HUDStartWatchingPacket extends IntegerCoordinatesPacket {

	public HUDStartWatchingPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new HUDStartWatchingPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof IWatchingHandler) {
			IWatchingHandler handler = (IWatchingHandler) pipe.pipe;
			handler.playerStartWatching(player, getInteger());
		}
	}
}

