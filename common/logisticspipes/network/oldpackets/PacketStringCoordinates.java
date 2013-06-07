package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PacketStringCoordinates extends PacketCoordinates {

	public String string;
	
	public PacketStringCoordinates() {
		super();
	}
	
	public PacketStringCoordinates(int id, int xCoord, int yCoord, int zCoord, String value) {
		super(id, xCoord, yCoord, zCoord);
		string = value;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeUTF(string);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		string = data.readUTF();
	}
}
