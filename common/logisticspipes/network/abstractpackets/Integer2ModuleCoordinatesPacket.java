package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class Integer2ModuleCoordinatesPacket extends IntegerModuleCoordinatesPacket {

	@Getter
	@Setter
	private int integer2;

	public Integer2ModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		setInteger2(data.readInt());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getInteger2());
	}
}
