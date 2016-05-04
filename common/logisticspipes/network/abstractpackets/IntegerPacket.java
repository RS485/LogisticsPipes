package logisticspipes.network.abstractpackets;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class IntegerPacket extends ModernPacket {

	@Getter
	@Setter
	private int integer;

	public IntegerPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		setInteger(input.readInt());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(getInteger());
	}
}
