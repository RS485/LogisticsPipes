package logisticspipes.interfaces;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

public interface IClientState {

	void writeData(LPDataOutputStream data) throws IOException;

	void readData(LPDataInputStream data) throws IOException;
}
