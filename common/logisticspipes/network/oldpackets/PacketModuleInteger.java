package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketModuleInteger extends PacketPipeInteger {
	
	public int slot;
	
	public PacketModuleInteger() {
		super();
	}

	public PacketModuleInteger(int id, int x, int y, int z, int slot, int integer) {
		super(id, x, y, z, integer);
		this.slot = slot;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(slot);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readInt();
	}
}
