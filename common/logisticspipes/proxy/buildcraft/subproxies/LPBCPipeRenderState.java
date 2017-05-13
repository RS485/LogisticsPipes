/*
package logisticspipes.proxy.buildcraft.subproxies;

import buildcraft.transport.PipeRenderState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class LPBCPipeRenderState extends PipeRenderState implements IBCRenderState {

	@Override
	public void writeData_LP(LPDataOutput output) {
		output.writeBoolean(true);
		ByteBuf buf = Unpooled.buffer(128);
		writeData(buf);
		output.writeByteBuf(buf);
	}

	@Override
	public void readData_LP(LPDataInput input) {
		if (input.readBoolean()) {
			ByteBuf buf = input.readByteBuf();
			readData(buf);
		}
	}
}
*/
