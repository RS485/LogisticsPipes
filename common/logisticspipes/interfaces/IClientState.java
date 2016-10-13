package logisticspipes.interfaces;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public interface IClientState {

	void writeData(LPDataOutput output);

	void readData(LPDataInput input);
}
