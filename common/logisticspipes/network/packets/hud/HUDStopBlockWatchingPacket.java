package logisticspipes.network.packets.hud;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
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
		IBlockWatchingHandler tile = this.getTileAs(player.world, IBlockWatchingHandler.class);
		if (tile != null) {
			tile.playerStopWatching(player);
		}
	}
}
