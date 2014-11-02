package logisticspipes.proxy.buildcraft.bc61.subproxies;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderState;
import logisticspipes.renderer.state.ConnectionMatrix;
import buildcraft.transport.utils.FacadeMatrix;
import buildcraft.transport.utils.GateMatrix;
import buildcraft.transport.utils.RobotStationMatrix;
import buildcraft.transport.utils.WireMatrix;

public class BCRenderState implements IBCRenderState {
	public WireMatrix wireMatrix = new WireMatrix();
	public final ConnectionMatrix plugMatrix = new ConnectionMatrix();
	public final RobotStationMatrix robotStationMatrix = new RobotStationMatrix();
	public final FacadeMatrix facadeMatrix = new FacadeMatrix();
	public final GateMatrix gateMatrix = new GateMatrix();

	private boolean dirty = false;
	private int gateIconIndex = 0;

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
		return dirty || wireMatrix.isDirty() || facadeMatrix.isDirty() || plugMatrix.isDirty() || robotStationMatrix.isDirty() || gateMatrix.isDirty();
	}

	@Override
	public boolean needsRenderUpdate() {
		return facadeMatrix.isDirty() || plugMatrix.isDirty() || robotStationMatrix.isDirty();
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(gateIconIndex);
		plugMatrix.writeData(data);
		
		ByteBuf buf = Unpooled.buffer();
		
		wireMatrix.writeData(buf);
		facadeMatrix.writeData(buf);
		robotStationMatrix.writeData(buf);
		gateMatrix.writeData(buf);
		
		data.writeByteArray(buf.array());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		gateIconIndex = data.readInt();
		plugMatrix.readData(data);
		
		ByteBuf buf = Unpooled.copiedBuffer(data.readByteArray());
		
		wireMatrix.readData(buf);
		facadeMatrix.readData(buf);
		robotStationMatrix.readData(buf);
		gateMatrix.readData(buf);
	}
}
