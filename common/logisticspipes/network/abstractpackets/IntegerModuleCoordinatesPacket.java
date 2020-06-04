package logisticspipes.network.abstractpackets;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class IntegerModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	private int integer;

	public IntegerModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		setInteger(input.readInt());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(getInteger());
	}

	public int getInteger() {
		return this.integer;
	}

	public IntegerModuleCoordinatesPacket setInteger(int integer) {
		this.integer = integer;
		return this;
	}
}
