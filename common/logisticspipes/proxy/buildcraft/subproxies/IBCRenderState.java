package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

public interface IBCRenderState {

	boolean needsRenderUpdate();

	boolean isDirty();

	void writeData_LP(LPDataOutputStream data) throws IOException;

	void readData_LP(LPDataInputStream data) throws IOException;

	void clean();

}
