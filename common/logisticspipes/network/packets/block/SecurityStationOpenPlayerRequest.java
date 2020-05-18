package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringCoordinatesPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SecurityStationOpenPlayerRequest extends StringCoordinatesPacket {

	public SecurityStationOpenPlayerRequest(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SecurityStationOpenPlayerRequest(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsSecurityTileEntity tile = this.getTileAs(player.world, LogisticsSecurityTileEntity.class);
		if (tile != null) {
			if (getString() != null && !getString().isEmpty()) {
				tile.handleOpenSecurityPlayer(player, getString());
			}
		}
	}
}
