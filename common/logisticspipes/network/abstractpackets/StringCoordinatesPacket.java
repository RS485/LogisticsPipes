package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public abstract class StringCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private String string;
	
	public StringCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeUTF(getString());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		setString(data.readUTF());
	}
}
