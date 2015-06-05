package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class IntegerModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int integer;

	public IntegerModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		setInteger(data.readInt());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getInteger());
	}
}
