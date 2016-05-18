package logisticspipes.proxy.buildcraft.subproxies;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public interface IBCRenderState {

	boolean needsRenderUpdate();

	boolean isDirty();

	void writeData_LP(LPDataOutput output);

	void readData_LP(LPDataInput input);

	void clean();

}
