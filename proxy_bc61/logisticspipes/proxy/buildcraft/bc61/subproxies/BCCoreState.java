package logisticspipes.proxy.buildcraft.bc61.subproxies;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.buildcraft.subproxies.IBCCoreState;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.transport.gates.ItemGate;

public class BCCoreState implements IBCCoreState {

	public ItemGate.GatePluggable[] gates = new ItemGate.GatePluggable[ForgeDirection.VALID_DIRECTIONS.length];

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			ItemGate.GatePluggable gate = gates[i];

			boolean gateValid = gate != null;
			data.writeBoolean(gateValid);
			if (gateValid) {
				ByteBuf buffer = Unpooled.buffer();
				gate.writeToByteByf(buffer);
				data.writeByteArray(buffer.array());
			}
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (data.readBoolean()) {
				ItemGate.GatePluggable gate = gates[i];
				if (gate == null) {
					gates[i] = gate = new ItemGate.GatePluggable();
				}
				ByteBuf buffer = Unpooled.copiedBuffer(data.readByteArray());
				gate.readFromByteBuf(buffer);
			} else {
				gates[i] = null;
			}
		}
	}

	
	@Override
	public Object getOriginal() {
		return this;
	}
}
