package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

public interface IBCRenderState {
	Object getOriginal();
	void clean();
	boolean isDirty();
	boolean needsRenderUpdate();
	void writeData(LPDataOutputStream data) throws IOException;
	void readData(LPDataInputStream data) throws IOException;
	boolean isGatePulsing();
	boolean isGateLit();
	void setIsGateLit(boolean gateActive);
	void setIsGatePulsing(boolean gateActive);
}
