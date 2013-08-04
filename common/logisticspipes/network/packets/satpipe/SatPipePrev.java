package logisticspipes.network.packets.satpipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

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

		if (pipe.pipe instanceof PipeItemsSatelliteLogistics) {
			((PipeItemsSatelliteLogistics) pipe.pipe).setPrevId(player);
		}
		if (pipe.pipe instanceof PipeFluidSatellite) {
			((PipeFluidSatellite) pipe.pipe).setPrevId(player);
		}
	}

}

