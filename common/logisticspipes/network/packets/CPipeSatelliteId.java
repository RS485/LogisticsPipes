package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;
import logisticspipes.network.packets.abstracts.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class CPipeSatelliteId extends CoordinatesPacket {

	@Getter
	@Setter
	private int pipeId;
	
	public CPipeSatelliteId(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeSatelliteId(getID());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(pipeId);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		pipeId = data.readInt();
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

		((BaseLogicCrafting) pipe.pipe.logic).setSatelliteId(pipeId, -1);
	}
}
