package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(travelId);
		output.writeFloat(speed);
		output.writeFloat(position);
		output.writeFacing(input);
		output.writeFacing(this.output);
		output.writeFloat(yaw);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		travelId = input.readInt();
		speed = input.readFloat();
		position = input.readFloat();
		this.input = input.readFacing();
		output = input.readFacing();
		yaw = input.readFloat();
	}

	@Override
	public ModernPacket template() {
		return new PipePositionPacket(getId());
	}
}
