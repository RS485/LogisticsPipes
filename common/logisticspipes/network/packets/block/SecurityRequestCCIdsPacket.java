package logisticspipes.network.packets.block;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;

public class SecurityRequestCCIdsPacket extends CoordinatesPacket {

	public SecurityRequestCCIdsPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SecurityRequestCCIdsPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsSecurityTileEntity tile = this.getTile(player.worldObj, LogisticsSecurityTileEntity.class);
		if(tile != null) {
			tile.requestList(player);
		}
	}
}

