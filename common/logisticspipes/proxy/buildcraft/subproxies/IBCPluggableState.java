package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import logisticspipes.interfaces.IClientState;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

public interface IBCPluggableState extends IClientState {

	@Override
	void writeData(LPDataOutputStream data) throws IOException;

	@Override
	void readData(LPDataInputStream data) throws IOException;

	boolean isDirty(boolean clean);
}
