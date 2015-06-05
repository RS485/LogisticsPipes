package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import buildcraft.transport.PipeRenderState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LPBCPipeRenderState extends PipeRenderState implements IBCRenderState {

	@Override
	public void writeData_LP(LPDataOutputStream data) throws IOException {
		ByteBuf buf = Unpooled.buffer(128);
		writeData(buf);
		data.writeByteBuf(buf);
	}

	@Override
	public void readData_LP(LPDataInputStream data) throws IOException {
		ByteBuf buf = data.readByteBuf();
		readData(buf);
	}
}
