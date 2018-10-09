package logisticspipes.network.packets.satpipe;

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

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SatPipeSetID extends CoordinatesPacket {

	@Getter
	@Setter
	private int satID;

	public SatPipeSetID(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SatPipeSetID(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeInt(satID);
		super.writeData(output);
	}

	@Override
	public void readData(LPDataInput input) {
		satID = input.readInt();
		super.readData(input);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.world);
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
