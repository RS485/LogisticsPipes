package logisticspipes.network.abstractpackets;

import java.io.IOException;
import java.util.BitSet;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class BitSetCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private BitSet flags;

	public BitSetCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBitSet(getFlags());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		setFlags(data.readBitSet());
	}
}
