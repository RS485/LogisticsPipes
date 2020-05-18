package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class PowerJunctionCheatPacket extends CoordinatesPacket {

	public PowerJunctionCheatPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PowerJunctionCheatPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (!LogisticsPipes.isDEBUG()) {
			return;
		}
		final LogisticsPowerJunctionTileEntity tile = this.getTileAs(player.world, LogisticsPowerJunctionTileEntity.class);
		if (tile != null) {
			tile.addEnergy(100000);
		}
	}
}
