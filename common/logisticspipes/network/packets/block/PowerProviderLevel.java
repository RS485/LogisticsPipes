package logisticspipes.network.packets.block;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.network.abstractpackets.FloatCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class PowerProviderLevel extends FloatCoordinatesPacket {

	public PowerProviderLevel(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PowerProviderLevel(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsPowerProviderTileEntity tile = this.getTile(player.worldObj, LogisticsPowerProviderTileEntity.class);
		if (tile != null) {
			tile.handlePowerPacket(getFloat());
		}
	}
}
