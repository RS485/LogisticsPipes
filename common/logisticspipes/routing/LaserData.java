package logisticspipes.routing;

import java.util.EnumSet;

import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LaserData {

	@NonNull
	private BlockPos pos;
	@NonNull
	private Direction dir;
	@NonNull
	private EnumSet<PipeRoutingConnectionType> connectionType;
	private boolean finalPipe = true;
	private boolean startPipe = false;
	private int length = 1;

	public void writeData(PacketByteBuf output) {
		output.writeBlockPos(pos);
		output.writeEnumConstant(dir);
		output.writeBoolean(finalPipe);
		output.writeBoolean(startPipe);
		output.writeInt(length);
		for (PipeRoutingConnectionType type : PipeRoutingConnectionType.values()) {
			output.writeBoolean(connectionType.contains(type));
		}
	}

	public LaserData readData(PacketByteBuf input) {
		pos = input.readBlockPos();
		dir = input.readEnumConstant(Direction.class);
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
