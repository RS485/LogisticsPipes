package logisticspipes.network.abstractpackets;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class BooleanModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	boolean flag;

	public BooleanModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeBoolean(flag);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		flag = input.readBoolean();
	}
}
