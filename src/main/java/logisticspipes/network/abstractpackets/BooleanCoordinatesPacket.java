package logisticspipes.network.abstractpackets;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class BooleanCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	boolean flag;

	public BooleanCoordinatesPacket(int id) {
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
}
