package logisticspipes.network.packets.satpipe;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(satID);
		super.writeData(output);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		satID = input.readInt();
		super.readData(input);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (pipe.pipe instanceof PipeItemsSatelliteLogistics) {
			((PipeItemsSatelliteLogistics) pipe.pipe).setSatelliteId(getSatID());
		}
		if (pipe.pipe instanceof PipeFluidSatellite) {
			((PipeFluidSatellite) pipe.pipe).setSatelliteId(getSatID());
		}
	}

}
