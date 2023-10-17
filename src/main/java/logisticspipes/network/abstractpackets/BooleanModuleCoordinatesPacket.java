package logisticspipes.network.abstractpackets;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class BooleanModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	boolean flag;

	public BooleanModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(flag);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		flag = input.readBoolean();
	}

	public boolean isFlag() {
		return this.flag;
	}

	public BooleanModuleCoordinatesPacket setFlag(boolean flag) {
		this.flag = flag;
		return this;
	}
}
