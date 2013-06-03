package logisticspipes.network.packets;

import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayerMP;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;
import logisticspipes.network.packets.abstracts.ModernPacket;

public class SatPipeNext extends CoordinatesPacket {

	public SatPipeNext(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SatPipeNext(getID());
	}

	@Override
	public void processPacket(EntityPlayerMP player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (pipe.pipe.logic instanceof BaseLogicSatellite) {
			((BaseLogicSatellite) pipe.pipe.logic).setNextId(player);
		}
		if (pipe.pipe.logic instanceof BaseLogicLiquidSatellite) {
			((BaseLogicLiquidSatellite) pipe.pipe.logic).setNextId(player);
		}
	}
	
}
