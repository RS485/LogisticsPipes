package logisticspipes.network.packets.satpipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

public class SatPipeNext extends CoordinatesPacket {

	public SatPipeNext(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SatPipeNext(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (pipe.pipe instanceof PipeItemsSatelliteLogistics) {
			((PipeItemsSatelliteLogistics) pipe.pipe).setNextId(player);
		}
		if (pipe.pipe instanceof PipeFluidSatellite) {
			((PipeFluidSatellite) pipe.pipe).setNextId(player);
		}
	}

}
