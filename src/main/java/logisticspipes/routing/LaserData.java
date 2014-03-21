package logisticspipes.routing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraftforge.common.ForgeDirection;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class LaserData {
	@NonNull private int posX;
	@NonNull private int posY;
	@NonNull private int posZ;
	@NonNull private ForgeDirection dir;
	@NonNull private EnumSet<PipeRoutingConnectionType> connectionType;
	private boolean finalPipe = true;
	private boolean startPipe = false;
	private int length = 1;

	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(posX);
		data.writeInt(posY);
		data.writeInt(posZ);
		data.writeByte(dir.ordinal());
		data.writeBoolean(finalPipe);
		data.writeBoolean(startPipe);
		data.writeInt(length);
		for(PipeRoutingConnectionType type: PipeRoutingConnectionType.values()) {
			data.writeBoolean(connectionType.contains(type));
		}
	}

	public LaserData readData(DataInputStream data) throws IOException {
		posX = data.readInt();
		posY = data.readInt();
		posZ = data.readInt();
		dir = ForgeDirection.values()[data.readByte()];
		finalPipe = data.readBoolean();
		startPipe = data.readBoolean();
		length = data.readInt();
		connectionType = EnumSet.noneOf(PipeRoutingConnectionType.class);
		for(PipeRoutingConnectionType type: PipeRoutingConnectionType.values()) {
			if(data.readBoolean()) {
				connectionType.add(type);
			}
		}
		return this;
	}
}
