package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPipeInteger extends PacketCoordinates {
	public int integer;

	public PacketPipeInteger() {
		super();
	}

	public PacketPipeInteger(int id, int x, int y, int z, int integer) {
		super(id, x, y, z);

		this.integer = integer;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		data.writeInt(integer);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		integer = data.readInt();
	}
}
