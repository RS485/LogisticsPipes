package logisticspipes.proxy.buildcraft.subproxies;

import java.io.IOException;
import java.util.Arrays;

import buildcraft.transport.PipePluggableState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;

import network.rs485.logisticspipes.util.LPDataIOWrapper;
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
		byte[] newBytes = LPDataIOWrapper.collectData(this::writeData);
		boolean result = !Arrays.equals(newBytes, oldBuffer);
		if (clean && result) {
			oldBuffer = newBytes;
		}
		return result;
	}
}
