package logisticspipes.network.packets.block;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;

public class SecurityAuthorizationPacket extends IntegerCoordinatesPacket {

	public SecurityAuthorizationPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SecurityAuthorizationPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsSecurityTileEntity tile = this.getTile(player.worldObj, LogisticsSecurityTileEntity.class);
		if(tile != null) {
			if(getInteger() == 1) {
				tile.authorizeStation();
			} else {
				tile.deauthorizeStation();
			}
		}
	}
}

