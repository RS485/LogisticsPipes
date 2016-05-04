package logisticspipes.interfaces;

import java.io.IOException;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public interface IClientState {

	void writeData(LPDataOutput output) throws IOException;

	void readData(LPDataInput input) throws IOException;
}
