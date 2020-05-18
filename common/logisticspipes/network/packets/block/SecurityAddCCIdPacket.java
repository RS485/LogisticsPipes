package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SecurityAddCCIdPacket extends IntegerCoordinatesPacket {

	public SecurityAddCCIdPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SecurityAddCCIdPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsSecurityTileEntity tile = this.getTileAs(player.world, LogisticsSecurityTileEntity.class);
		if (tile != null) {
			tile.addCCToList(getInteger());
			tile.requestList(player);
		}
	}
}
