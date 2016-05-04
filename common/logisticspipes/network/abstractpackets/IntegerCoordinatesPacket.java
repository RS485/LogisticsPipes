package logisticspipes.network.abstractpackets;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class IntegerCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private int integer;

	public IntegerCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		setInteger(input.readInt());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeInt(getInteger());
	}
}
