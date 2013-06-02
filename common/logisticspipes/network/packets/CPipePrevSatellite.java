package logisticspipes.network.packets;

import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayerMP;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;
import logisticspipes.network.packets.abstracts.ModernPacket;

public class CPipePrevSatellite extends CoordinatesPacket {

	public CPipePrevSatellite(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipePrevSatellite(getID());
	}

	@Override
	public void processPacket(EntityPlayerMP player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setPrevSatellite(player);
	}
	
}
