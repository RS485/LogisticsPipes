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
		byte[] array = toByteArray(flags);
		data.writeByte(array.length);
		data.write(array);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		byte size = data.readByte();
		byte[] array = new byte[size];
		data.read(array, 0, size);
		flags = fromByteArray(array);
	}
	
	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] toByteArray(BitSet bits) {
		byte[] bytes = new byte[3];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
			}
		}
		return bytes;
	}
}
