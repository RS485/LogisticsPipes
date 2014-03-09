package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public abstract class IntegerPacket extends ModernPacket {
	
	@Getter
	@Setter
	private int integer;

	public IntegerPacket(int id) {
		super(id);
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		setInteger(data.readInt());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(getInteger());
	}
}
