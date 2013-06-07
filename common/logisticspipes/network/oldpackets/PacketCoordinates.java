package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PacketCoordinates extends PacketLogisticsPipes {

	private int id;

	public int posX;
	public int posY;
	public int posZ;

	public PacketCoordinates() {
		super();
	}

	public PacketCoordinates(int id, int x, int y, int z) {
		super();

		this.id = id;

		posX = x;
		posY = y;
		posZ = z;
	}

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

	@Override
	public int getID() {
		return id;
	}

}
