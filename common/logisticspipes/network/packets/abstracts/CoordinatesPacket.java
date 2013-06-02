package logisticspipes.network.packets.abstracts;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class CoordinatesPacket extends ModernPacket<CoordinatesPacket> {

	public CoordinatesPacket(int id, int x, int y, int z) {
		super(id);

		posX = x;
		posY = y;
		posZ = z;
	}

	public int posX;
	public int posY;
	public int posZ;

	@Override
	public void writeData(DataOutputStream data) throws IOException {

		data.writeInt(posX);
		data.writeInt(posY);
		data.writeInt(posZ);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {

		posX = data.readInt();
		posY = data.readInt();
		posZ = data.readInt();

	}

}
