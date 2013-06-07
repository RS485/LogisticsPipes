package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PacketInteger extends PacketLogisticsPipes {
	
	public int id;
	public int value;
	
	public PacketInteger() {
		super();
	}
	
	public PacketInteger(int id, int integer) {
		this.id = id;
		this.value = integer;
	}
	
	@Override
	public int getID() {
		return id;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		value = data.readInt();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(value);
	}

}
