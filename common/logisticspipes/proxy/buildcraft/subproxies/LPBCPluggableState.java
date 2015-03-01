package logisticspipes.proxy.buildcraft.subproxies;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.Arrays;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import lombok.SneakyThrows;
import buildcraft.transport.PipePluggableState;

public class LPBCPluggableState extends PipePluggableState implements IBCPluggableState {
	
	private byte[] oldBuffer;
	
	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		ByteBuf buf = Unpooled.buffer(128);
		this.writeData(buf);
		data.writeByteBuf(buf);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		ByteBuf buf = data.readByteBuf();
		this.readData(buf);
	}
	
	@SneakyThrows({IOException.class})
	public boolean isDirty(boolean clean) {
		LPDataOutputStream buffer = new LPDataOutputStream();
		ByteBuf buf = Unpooled.buffer(128);
		this.writeData(buf);
		buffer.writeByteBuf(buf);
		byte[] newBuffer = buffer.toByteArray();
		boolean result = !Arrays.equals(newBuffer, oldBuffer);
		if(clean && result) {
			oldBuffer = newBuffer;
		}
		return result;
	}
}
