package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

public interface IBCRenderState {
	@IgnoreDisabledProxy
	Object getOriginal();
	void clean();
	boolean isDirty();
	boolean needsRenderUpdate();
	void writeData(LPDataOutputStream data) throws IOException;
	void readData(LPDataInputStream data) throws IOException;
	/** BC6.0 */
	boolean isGatePulsing();
	/** BC6.0 */
	boolean isGateLit();
	/** BC6.0 */
	void setIsGateLit(boolean gateActive);
	/** BC6.0 */
	void setIsGatePulsing(boolean gateActive);
}
