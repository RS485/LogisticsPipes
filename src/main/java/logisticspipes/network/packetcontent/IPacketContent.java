package logisticspipes.network.packetcontent;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public interface IPacketContent<T> {

	T getValue();

	void setValue(T value);

	void readData(LPDataInput input);

	void writeData(LPDataOutput output);
}
