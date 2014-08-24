package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

public interface IBCCoreState {
	void writeData(LPDataOutputStream data) throws IOException;
	void readData(LPDataInputStream data) throws IOException;
	Object getOriginal();
}
