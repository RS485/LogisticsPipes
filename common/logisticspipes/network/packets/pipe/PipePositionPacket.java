package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
	private ForgeDirection input;
	@Getter
	@Setter
	private ForgeDirection output;
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
		output.writeForgeDirection(input);
		output.writeForgeDirection(this.output);
		output.writeFloat(yaw);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		travelId = input.readInt();
		speed = input.readFloat();
		position = input.readFloat();
		this.input = input.readForgeDirection();
		output = input.readForgeDirection();
		yaw = input.readFloat();
	}

	@Override
	public ModernPacket template() {
		return new PipePositionPacket(getId());
	}
}
