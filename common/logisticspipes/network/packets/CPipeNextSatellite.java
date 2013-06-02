package logisticspipes.network.packets;

import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayerMP;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;

public class CPipeNextSatellite extends CoordinatesPacket<CPipeNextSatellite> {

	public CPipeNextSatellite(int id) {
		super(id);
	}

	@Override
	public CPipeNextSatellite template() {
		return new CPipeNextSatellite(getID());
	}

	@Override
	public void processPacket(EntityPlayerMP player) {
		final TileGenericPipe pipe = getPipe(player.worldObj, getPosX(), getPosY(), getPosZ());
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setNextSatellite(player);
	}
	
}
