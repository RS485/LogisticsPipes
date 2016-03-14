package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.util.EnumFacing;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PipePositionPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private int travelId;
	@Getter
	@Setter
	private float speed;
	@Getter
	@Setter
	private float position;
	@Getter
	@Setter
	private EnumFacing input;
	@Getter
	@Setter
	private EnumFacing output;
	@Getter
	@Setter
	private float yaw;

	public PipePositionPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.TRANSPORT);
		if (tile == null || tile.pipe == null || tile.pipe.transport == null) {
			return;
		}
		tile.pipe.transport.handleItemPositionPacket(travelId, input, output, speed, position, yaw);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(travelId);
		data.writeFloat(speed);
		data.writeFloat(position);
		data.writeEnumFacing(input);
		data.writeEnumFacing(output);
		data.writeFloat(yaw);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		travelId = data.readInt();
		speed = data.readFloat();
		position = data.readFloat();
		input = data.readEnumFacing();
		output = data.readEnumFacing();
		yaw = data.readFloat();
	}

	@Override
	public ModernPacket template() {
		return new PipePositionPacket(getId());
	}
}
