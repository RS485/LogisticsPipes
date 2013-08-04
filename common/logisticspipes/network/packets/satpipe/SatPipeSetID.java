package logisticspipes.network.packets.satpipe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain = true)
public class SatPipeSetID extends CoordinatesPacket {

	public SatPipeSetID(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SatPipeSetID(getId());
	}

	@Getter
	@Setter
	private int satID;

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(satID);
		super.writeData(data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		satID = data.readInt();
		super.readData(data);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (pipe.pipe instanceof PipeItemsSatelliteLogistics) {
			((PipeItemsSatelliteLogistics) pipe.pipe).setSatelliteId(getSatID());
		}
		if (pipe.pipe instanceof PipeFluidSatellite) {
			((PipeFluidSatellite) pipe.pipe)
					.setSatelliteId(getSatID());
		}
	}

}

