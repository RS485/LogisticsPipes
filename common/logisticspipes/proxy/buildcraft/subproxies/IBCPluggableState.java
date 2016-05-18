package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.interfaces.IClientState;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public interface IBCPluggableState extends IClientState {

	@Override
	void writeData(LPDataOutput output);

	@Override
	void readData(LPDataInput input);

	boolean isDirty(boolean clean);
}
