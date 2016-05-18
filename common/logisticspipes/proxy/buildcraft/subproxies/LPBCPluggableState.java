package logisticspipes.proxy.buildcraft.subproxies;

import java.util.Arrays;

import buildcraft.transport.PipePluggableState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import network.rs485.logisticspipes.util.LPDataIOWrapper;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class LPBCPluggableState extends PipePluggableState implements IBCPluggableState {

	private byte[] oldBuffer;

	@Override
	public void writeData(LPDataOutput output) {
		ByteBuf buf = Unpooled.buffer(128);
		this.writeData(buf);
		output.writeByteBuf(buf);
	}

	@Override
	public void readData(LPDataInput input) {
		ByteBuf buf = input.readByteBuf();
		this.readData(buf);
	}

	@Override
	public boolean isDirty(boolean clean) {
		byte[] newBytes = LPDataIOWrapper.collectData(this::writeData);
		boolean result = !Arrays.equals(newBytes, oldBuffer);
		if (clean && result) {
			oldBuffer = newBytes;
		}
		return result;
	}
}
