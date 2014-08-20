package logisticspipes.renderer.state;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import net.minecraftforge.common.util.ForgeDirection;

public class TextureMatrix {

	private final int[] iconIndexes = new int[7];
	private boolean dirty = false;

	public int getTextureIndex(ForgeDirection direction) {
		return iconIndexes[direction.ordinal()];
	}

	public void setIconIndex(ForgeDirection direction, int value) {
		if (iconIndexes[direction.ordinal()] != value) {
			iconIndexes[direction.ordinal()] = value;
			dirty = true;
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(LPDataOutputStream data) throws IOException {
		for (int iconIndexe : iconIndexes) {
			data.writeByte(iconIndexe);
		}
	}

	public void readData(LPDataInputStream data) throws IOException {
		for (int i = 0; i < iconIndexes.length; i++) {
			int icon = data.readByte();
			if (iconIndexes[i] != icon) {
				iconIndexes[i] = icon;
				dirty = true;
			}
		}
	}
}