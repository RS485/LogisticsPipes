package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public abstract class Integer2CoordinatesPacket extends IntegerCoordinatesPacket {

	@Getter
	@Setter
	private int integer2;

	public Integer2CoordinatesPacket(int id) {
		super(id);
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		setInteger2(data.readInt());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getInteger2());
	}
}
