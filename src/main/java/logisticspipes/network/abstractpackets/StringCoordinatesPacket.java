package logisticspipes.network.abstractpackets;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class StringCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private String string;

	public StringCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeUTF(getString());
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		setString(input.readUTF());
	}
}
