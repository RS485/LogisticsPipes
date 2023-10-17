package logisticspipes.network.packetcontent;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class IntegerContent implements IPacketContent<Integer> {

	private int integer;

	@Override
	public Integer getValue() {
		return integer;
	}

	@Override
	public void setValue(Integer value) {
		integer = value;
	}

	@Override
	public void readData(LPDataInput input) {
		integer = input.readInt();
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeInt(integer);
	}
}
