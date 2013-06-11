package logisticspipes.network.packets.cpipe;

import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

public class CPipeNextSatellite extends CoordinatesPacket {

	public CPipeNextSatellite(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeNextSatellite(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setNextSatellite(player);
	}
	
}
