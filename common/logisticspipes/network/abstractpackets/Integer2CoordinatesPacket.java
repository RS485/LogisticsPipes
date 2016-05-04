package logisticspipes.network.abstractpackets;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class Integer2CoordinatesPacket extends IntegerCoordinatesPacket {

	@Getter
	@Setter
	private int integer2;

	public Integer2CoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		setInteger2(input.readInt());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeInt(getInteger2());
	}
}
