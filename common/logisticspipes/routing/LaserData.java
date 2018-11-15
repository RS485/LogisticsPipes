package logisticspipes.routing;

import java.util.EnumSet;

import net.minecraft.util.EnumFacing;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LaserData {

	private int posX;
	private int posY;
	private int posZ;
	@NonNull
	private EnumFacing dir;
	@NonNull
	private EnumSet<PipeRoutingConnectionType> connectionType;
	private boolean finalPipe = true;
	private boolean startPipe = false;
	private int length = 1;

	public void writeData(LPDataOutput output) {
		output.writeInt(posX);
		output.writeInt(posY);
		output.writeInt(posZ);
		output.writeFacing(dir);
		output.writeBoolean(finalPipe);
		output.writeBoolean(startPipe);
		output.writeInt(length);
		for (PipeRoutingConnectionType type : PipeRoutingConnectionType.values()) {
			output.writeBoolean(connectionType.contains(type));
		}
	}

	public LaserData readData(LPDataInput input) {
		posX = input.readInt();
		posY = input.readInt();
		posZ = input.readInt();
		dir = input.readFacing();
		finalPipe = input.readBoolean();
		startPipe = input.readBoolean();
		length = input.readInt();
		connectionType = EnumSet.noneOf(PipeRoutingConnectionType.class);
		for (PipeRoutingConnectionType type : PipeRoutingConnectionType.values()) {
			if (input.readBoolean()) {
				connectionType.add(type);
			}
		}
		return this;
	}
}
