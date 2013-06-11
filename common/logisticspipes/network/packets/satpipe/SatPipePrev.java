package logisticspipes.network.packets.satpipe;

import buildcraft.transport.TileGenericPipe;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;

public class SatPipePrev extends CoordinatesPacket {

	public SatPipePrev(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SatPipePrev(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (pipe.pipe.logic instanceof BaseLogicSatellite) {
			((BaseLogicSatellite) pipe.pipe.logic).setPrevId(player);
		}
		if (pipe.pipe.logic instanceof BaseLogicLiquidSatellite) {
			((BaseLogicLiquidSatellite) pipe.pipe.logic).setPrevId(player);
		}
	}

}
