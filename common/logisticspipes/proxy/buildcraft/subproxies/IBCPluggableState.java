package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import logisticspipes.interfaces.IClientState;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public interface IBCPluggableState extends IClientState {

	@Override
	void writeData(LPDataOutput output) throws IOException;

	@Override
	void readData(LPDataInput input) throws IOException;

	boolean isDirty(boolean clean);
}
