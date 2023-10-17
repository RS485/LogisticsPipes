package logisticspipes.network.abstractpackets;

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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBitSet(getFlags());
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		setFlags(input.readBitSet());
	}
}
