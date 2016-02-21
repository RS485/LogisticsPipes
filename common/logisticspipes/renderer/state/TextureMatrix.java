package logisticspipes.renderer.state;

import java.io.IOException;

import logisticspipes.config.Configs;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;

import net.minecraft.util.EnumFacing;

import lombok.Getter;

public class TextureMatrix {

	//Old Pipe Renderer
	private final int[] iconIndexes = new int[7];

	//New Pipe Renderer
	@Getter
	private int textureIndex;
	@Getter
	private boolean isRouted;
	private boolean[] isRoutedInDir = new boolean[6];
	private boolean[] isSubPowerInDir = new boolean[6];
	@Getter
	private boolean hasPowerUpgrade;
	@Getter
	private boolean hasPower;
	@Getter
	private boolean isFluid;
	@Getter
	private EnumFacing pointedOrientation;

	private boolean dirty = false;

	public int getTextureIndex(EnumFacing direction) {
		return iconIndexes[direction.ordinal()];
	}

	public void setIconIndex(EnumFacing direction, int value) {
		if (iconIndexes[direction.ordinal()] != value) {
			iconIndexes[direction.ordinal()] = value;
			dirty = true;
		}
	}

	public void refreshStates(CoreUnroutedPipe pipe) {
		if (textureIndex != pipe.getTextureIndex()) {
			dirty = true;
		}
		textureIndex = pipe.getTextureIndex();
		if (isRouted != pipe.isRoutedPipe()) {
			dirty = true;
		}
		isRouted = pipe.isRoutedPipe();
		if (isRouted) {
			CoreRoutedPipe cPipe = (CoreRoutedPipe) pipe;
			for (int i = 0; i < 6; i++) {
				if (isRoutedInDir[i] != cPipe.getRouter().isRoutedExit(UtilEnumFacing.getOrientation(i))) {
					dirty = true;
				}
				isRoutedInDir[i] = cPipe.getRouter().isRoutedExit(UtilEnumFacing.getOrientation(i));
			}
			for (int i = 0; i < 6; i++) {
				if (isSubPowerInDir[i] != cPipe.getRouter().isSubPoweredExit(UtilEnumFacing.getOrientation(i))) {
					dirty = true;
				}
				isSubPowerInDir[i] = cPipe.getRouter().isSubPoweredExit(UtilEnumFacing.getOrientation(i));
			}
			if (hasPowerUpgrade != (cPipe.getUpgradeManager().hasRFPowerSupplierUpgrade() || cPipe.getUpgradeManager().getIC2PowerLevel() > 0)) {
				dirty = true;
			}
			hasPowerUpgrade = cPipe.getUpgradeManager().hasRFPowerSupplierUpgrade() || cPipe.getUpgradeManager().getIC2PowerLevel() > 0;
			if (hasPower != (cPipe._textureBufferPowered || Configs.LOGISTICS_POWER_USAGE_DISABLED)) {
				dirty = true;
			}
			hasPower = cPipe._textureBufferPowered || Configs.LOGISTICS_POWER_USAGE_DISABLED;
			if (isFluid != cPipe.isFluidPipe()) {
				dirty = true;
			}
			isFluid = cPipe.isFluidPipe();
			if (pointedOrientation != cPipe.getPointedOrientation()) {
				dirty = true;
			}
			pointedOrientation = cPipe.getPointedOrientation();
		} else {
			isRoutedInDir = new boolean[6];
		}
	}

	public boolean isRoutedInDir(EnumFacing dir) {
		if (dir == UtilEnumFacing.UNKNOWN) {
			return false;
		}
		return isRoutedInDir[dir.ordinal()];
	}

	public boolean isSubPowerInDir(EnumFacing dir) {
		if (dir == UtilEnumFacing.UNKNOWN) {
			return false;
		}
		return isSubPowerInDir[dir.ordinal()];
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
		data.writeInt(textureIndex);
		data.writeBoolean(isRouted);
		data.writeBooleanArray(isRoutedInDir);
		data.writeBooleanArray(isSubPowerInDir);
		data.writeBoolean(hasPowerUpgrade);
		data.writeBoolean(hasPower);
		data.writeBoolean(isFluid);
		data.writeEnumFacing(pointedOrientation);
	}

	public void readData(LPDataInputStream data) throws IOException {
		for (int i = 0; i < iconIndexes.length; i++) {
			int icon = data.readByte();
			if (iconIndexes[i] != icon) {
				iconIndexes[i] = icon;
				dirty = true;
			}
		}
		textureIndex = data.readInt();
		isRouted = data.readBoolean();
		isRoutedInDir = data.readBooleanArray();
		isSubPowerInDir = data.readBooleanArray();
		hasPowerUpgrade = data.readBoolean();
		hasPower = data.readBoolean();
		isFluid = data.readBoolean();
		pointedOrientation = data.readEnumFacing();
	}
}
