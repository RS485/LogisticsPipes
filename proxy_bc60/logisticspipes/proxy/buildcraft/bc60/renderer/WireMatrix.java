package logisticspipes.proxy.buildcraft.bc60.renderer;

import java.io.IOException;
import java.util.BitSet;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.renderer.state.ConnectionMatrix;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.PipeWire;

public class WireMatrix {

	//private final boolean[] _hasWire = new boolean[IPipe.WireColor.values().length];
	private BitSet hasWire = new BitSet(PipeWire.values().length);

	private final ConnectionMatrix[] wires = new ConnectionMatrix[PipeWire.values().length];
	private final int[] wireIconIndex = new int[PipeWire.values().length];

	private boolean dirty = false;

	public WireMatrix() {
		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i] = new ConnectionMatrix();
		}
	}

	public boolean hasWire(PipeWire color) {
		return hasWire.get(color.ordinal());
	}

	public void setWire(PipeWire color, boolean value) {
		if (hasWire.get(color.ordinal()) != value) {
			hasWire.set(color.ordinal(), value);
			dirty = true;
		}
	}

	public boolean isWireConnected(PipeWire color, ForgeDirection direction) {
		return wires[color.ordinal()].isConnected(direction);
	}

	public void setWireConnected(PipeWire color, ForgeDirection direction, boolean value) {
		wires[color.ordinal()].setConnected(direction, value);
	}

	public int getWireIconIndex(PipeWire color) {
		return wireIconIndex[color.ordinal()];
	}

	public void setWireIndex(PipeWire color, int value) {
		if (wireIconIndex[color.ordinal()] != value) {
			wireIconIndex[color.ordinal()] = value;
			dirty = true;
		}
	}

	public boolean isDirty() {

		for (int i = 0; i < PipeWire.values().length; i++) {
			if (wires[i].isDirty()) {
				return true;
			}
		}

		return dirty;
	}

	public void clean() {
		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i].clean();
		}
		dirty = false;
	}

	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeBitSet(hasWire);

		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i].writeData(data);
			data.writeByte(wireIconIndex[i]);
		}
	}

	public void readData(LPDataInputStream data) throws IOException {
		hasWire = data.readBitSet();
		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i].readData(data);
			wireIconIndex[i] = data.readByte();
		}
	}
}