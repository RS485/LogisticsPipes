package logisticspipes.renderer.state;

import net.minecraft.util.EnumFacing;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class ConnectionMatrix {

	private int mask = 0;
	private int isBCPipeMask = 0;
	private int isTDPipeMask = 0;
	private boolean dirty = false;

	public boolean isConnected(EnumFacing direction) {
		// test if the direction.ordinal()'th bit of mask is set
		return (mask & (1 << direction.ordinal())) != 0;
	}

	public void setConnected(EnumFacing direction, boolean value) {
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

	public boolean isBCConnected(EnumFacing direction) {
		// test if the direction.ordinal()'th bit of mask is set
		return direction != null && (isBCPipeMask & (1 << direction.ordinal())) != 0;
	}

	public void setBCConnected(EnumFacing direction, boolean value) {
		if (isBCConnected(direction) != value) {
			// invert the direction.ordinal()'th bit of mask
			isBCPipeMask ^= 1 << direction.ordinal();
			dirty = true;
		}
	}

	public boolean isTDConnected(EnumFacing direction) {
		// test if the direction.ordinal()'th bit of mask is set
		return direction != null && (isTDPipeMask & (1 << direction.ordinal())) != 0;
	}

	public void setTDConnected(EnumFacing direction, boolean value) {
		if (isTDConnected(direction) != value) {
			// invert the direction.ordinal()'th bit of mask
			isTDPipeMask ^= 1 << direction.ordinal();
			dirty = true;
		}
	}

	/**
	 * Return a mask representing the connectivity for all sides.
	 *
	 * @return mask in EnumFacing order, least significant bit = first entry
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

	public void writeData(LPDataOutput output) {
		output.writeByte(mask);
		output.writeByte(isBCPipeMask);
		output.writeByte(isTDPipeMask);
	}

	public void readData(LPDataInput input) {
		byte newMask = input.readByte();

		if (newMask != mask) {
			mask = newMask;
			dirty = true;
		}

		newMask = input.readByte();
		if (newMask != isBCPipeMask) {
			isBCPipeMask = newMask;
			dirty = true;
		}

		newMask = input.readByte();
		if (newMask != isTDPipeMask) {
			isTDPipeMask = newMask;
			dirty = true;
		}
	}
}
