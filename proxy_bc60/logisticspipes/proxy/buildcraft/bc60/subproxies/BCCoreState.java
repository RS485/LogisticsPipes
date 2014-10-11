package logisticspipes.proxy.buildcraft.bc60.subproxies;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.buildcraft.subproxies.IBCCoreState;

public class BCCoreState implements IBCCoreState {

	public int gateMaterial = -1;
	public int gateLogic = -1;
	public final Set<Byte> expansions = new HashSet<Byte>();

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeByte(gateMaterial);
		data.writeByte(gateLogic);
		data.writeByte(expansions.size());
		for (Byte expansion : expansions) {
			data.writeByte(expansion);
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		gateMaterial = data.readByte();
		gateLogic = data.readByte();
		expansions.clear();
		int numExp = data.readByte();
		for (int i = 0; i < numExp; i++) {
			expansions.add(data.readByte());
		}
	}
	
	@Override
	public Object getOriginal() {
		return this;
	}
}
