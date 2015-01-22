package logisticspipes.proxy.buildcraft.subproxies;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import buildcraft.transport.PipeRenderState;

public class LPBCPipeRenderState extends PipeRenderState implements IBCRenderState {

	@Override
	public void writeData_LP(LPDataOutputStream data) throws IOException {
		ByteBuf buf = Unpooled.buffer(128);
		this.writeData(buf);
		data.writeByteBuf(buf);
	}

	@Override
	public void readData_LP(LPDataInputStream data) throws IOException {
		ByteBuf buf = data.readByteBuf();
		this.readData(buf);
	}
}
