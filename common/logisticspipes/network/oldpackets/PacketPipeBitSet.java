package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

import logisticspipes.network.BitSetHelper;

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
		BitSetHelper.write(data, flags);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		flags = BitSetHelper.read(data);
	}
}
