package logisticspipes.network.abstractpackets;

import java.io.IOException;
import java.util.BitSet;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class BitSetCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private BitSet flags;

	public BitSetCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeBitSet(getFlags());
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		setFlags(input.readBitSet());
	}
}
