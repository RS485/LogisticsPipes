package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class HUDStartBlockWatchingPacket extends CoordinatesPacket {

	public HUDStartBlockWatchingPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new HUDStartBlockWatchingPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IBlockWatchingHandler tile = this.getTile(player.world, IBlockWatchingHandler.class);
		if (tile != null) {
			tile.playerStartWatching(player);
		}
	}
}
