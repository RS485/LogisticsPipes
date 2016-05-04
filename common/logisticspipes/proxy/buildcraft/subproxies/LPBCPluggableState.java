package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;
import java.util.Arrays;

import buildcraft.transport.PipePluggableState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;

import logisticspipes.network.LPDataOutputStream;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class LPBCPluggableState extends PipePluggableState implements IBCPluggableState {

	private byte[] oldBuffer;

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		ByteBuf buf = Unpooled.buffer(128);
		this.writeData(buf);
		output.writeByteBuf(buf);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		ByteBuf buf = input.readByteBuf();
		this.readData(buf);
	}

	@Override
	@SneakyThrows({ IOException.class })
	public boolean isDirty(boolean clean) {
		LPDataOutputStream buffer = new LPDataOutputStream();
		ByteBuf buf = Unpooled.buffer(128);
		this.writeData(buf);
		buffer.writeByteBuf(buf);
		byte[] newBuffer = buffer.toByteArray();
		boolean result = !Arrays.equals(newBuffer, oldBuffer);
		if (clean && result) {
			oldBuffer = newBuffer;
		}
		return result;
	}
}
