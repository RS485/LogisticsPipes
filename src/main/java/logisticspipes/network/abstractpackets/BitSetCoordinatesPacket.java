package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

import logisticspipes.network.BitSetHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public abstract class BitSetCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private BitSet flags;
	
	public BitSetCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		BitSetHelper.write(data, getFlags());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		setFlags(BitSetHelper.read(data));
	}
}
