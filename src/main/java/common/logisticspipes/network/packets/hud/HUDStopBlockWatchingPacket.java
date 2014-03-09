package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;

public class HUDStopBlockWatchingPacket extends CoordinatesPacket {

	public HUDStopBlockWatchingPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new HUDStopBlockWatchingPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IBlockWatchingHandler tile = this.getTile(player.worldObj, IBlockWatchingHandler.class);
		if(tile != null) {
			tile.playerStopWatching(player);
		}
	}
}

