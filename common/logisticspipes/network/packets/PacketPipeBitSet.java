package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

public class PacketPipeBitSet extends PacketCoordinates {
	
	public BitSet flags;
	
	public PacketPipeBitSet(int id, int xCoord, int yCoord, int zCoord, BitSet flags) {
		super(id, xCoord, yCoord, zCoord);
		this.flags = flags;
	}

	public PacketPipeBitSet() {
		super();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		byte[] array = flags.toByteArray();
		data.writeByte(array.length);
		data.write(array);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		byte size = data.readByte();
		byte[] array = new byte[size];
		data.read(array, 0, size);
		flags = BitSet.valueOf(array);
	}
	
}
