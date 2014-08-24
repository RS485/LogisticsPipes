package logisticspipes.proxy.buildcraft.bc60.subproxies;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.renderer.FacadeMatrix;
import logisticspipes.proxy.buildcraft.renderer.WireMatrix;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderState;
import logisticspipes.renderer.state.ConnectionMatrix;

public class BCRenderState implements IBCRenderState {
	public WireMatrix wireMatrix = new WireMatrix();
	public final ConnectionMatrix plugMatrix = new ConnectionMatrix();
	public final ConnectionMatrix robotStationMatrix = new ConnectionMatrix();
	public final FacadeMatrix facadeMatrix = new FacadeMatrix();

	private boolean dirty = false;
	private boolean isGateLit = false;
	private boolean isGatePulsing = false;
	private int gateIconIndex = 0;

	public void setIsGateLit(boolean value) {
		if (isGateLit != value) {
			isGateLit = value;
			dirty = true;
		}
	}

	public boolean isGateLit() {
		return isGateLit;
	}

	public void setIsGatePulsing(boolean value) {
		if (isGatePulsing != value) {
			isGatePulsing = value;
			dirty = true;
		}
	}

	public boolean isGatePulsing() {
		return isGatePulsing;
	}

	@Override
	public Object getOriginal() {
		return this;
	}

	@Override
	public void clean() {
		dirty = false;
		facadeMatrix.clean();
		wireMatrix.clean();
		plugMatrix.clean();
		robotStationMatrix.clean();
	}

	@Override
	public boolean isDirty() {
		return dirty || wireMatrix.isDirty() || facadeMatrix.isDirty() || plugMatrix.isDirty() || robotStationMatrix.isDirty();
	}

	@Override
	public boolean needsRenderUpdate() {
		return facadeMatrix.isDirty() || plugMatrix.isDirty() || robotStationMatrix.isDirty();
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeBoolean(isGateLit);
		data.writeBoolean(isGatePulsing);
		data.writeInt(gateIconIndex);
		wireMatrix.writeData(data);
		facadeMatrix.writeData(data);
		plugMatrix.writeData(data);
		robotStationMatrix.writeData(data);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		isGateLit = data.readBoolean();
		isGatePulsing = data.readBoolean();
		gateIconIndex = data.readInt();
		wireMatrix.readData(data);
		facadeMatrix.readData(data);
		plugMatrix.readData(data);
		robotStationMatrix.readData(data);
	}
}
