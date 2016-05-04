package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public interface IBCRenderState {

	boolean needsRenderUpdate();

	boolean isDirty();

	void writeData_LP(LPDataOutput output) throws IOException;

	void readData_LP(LPDataInput input) throws IOException;

	void clean();

}
