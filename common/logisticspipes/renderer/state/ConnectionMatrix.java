package logisticspipes.renderer.state;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import net.minecraftforge.common.util.ForgeDirection;

public class ConnectionMatrix {

	private int mask = 0;
	private int isBCPipeMask = 0;
	private int isTDPipeMask = 0;
	private boolean dirty = false;

	public boolean isConnected(ForgeDirection direction) {
		// test if the direction.ordinal()'th bit of mask is set
		return (mask & (1 << direction.ordinal())) != 0;
	}

	public void setConnected(ForgeDirection direction, boolean value) {
		if (isConnected(direction) != value) {
			// invert the direction.ordinal()'th bit of mask
			mask ^= 1 << direction.ordinal();
			dirty = true;
		}
		if (!value) {
			setBCConnected(direction, false);
			setTDConnected(direction, false);
		}
	}

	public boolean isBCConnected(ForgeDirection direction) {
		// test if the direction.ordinal()'th bit of mask is set
		return (isBCPipeMask & (1 << direction.ordinal())) != 0;
	}

	public void setBCConnected(ForgeDirection direction, boolean value) {
		if (isBCConnected(direction) != value) {
			// invert the direction.ordinal()'th bit of mask
			isBCPipeMask ^= 1 << direction.ordinal();
			dirty = true;
		}
	}

	public boolean isTDConnected(ForgeDirection direction) {
		// test if the direction.ordinal()'th bit of mask is set
		return (isTDPipeMask & (1 << direction.ordinal())) != 0;
	}

	public void setTDConnected(ForgeDirection direction, boolean value) {
		if (isTDConnected(direction) != value) {
			// invert the direction.ordinal()'th bit of mask
			isTDPipeMask ^= 1 << direction.ordinal();
			dirty = true;
		}
	}

	/**
	 * Return a mask representing the connectivity for all sides.
	 *
	 * @return mask in ForgeDirection order, least significant bit = first entry
	 */
	public int getMask() {
		return mask;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeByte(mask);
		data.writeByte(isBCPipeMask);
		data.writeByte(isTDPipeMask);
	}

	public void readData(LPDataInputStream data) throws IOException {
		byte newMask = data.readByte();

		if (newMask != mask) {
			mask = newMask;
			dirty = true;
		}

		newMask = data.readByte();
		if (newMask != isBCPipeMask) {
			isBCPipeMask = newMask;
			dirty = true;
		}

		newMask = data.readByte();
		if (newMask != isTDPipeMask) {
			isTDPipeMask = newMask;
			dirty = true;
		}
	}
}
