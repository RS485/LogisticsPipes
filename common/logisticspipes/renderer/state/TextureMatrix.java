package logisticspipes.renderer.state;

import net.minecraft.util.EnumFacing;

import lombok.Getter;

import logisticspipes.config.Configs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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

	private boolean dirty = true;

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
		if (isRouted != pipe.isRouted()) {
			dirty = true;
		}
		isRouted = pipe.isRouted();
		if (isRouted) {
			CoreRoutedPipe cPipe = (CoreRoutedPipe) pipe;
			for (int i = 0; i < 6; i++) {
				if (isRoutedInDir[i] != cPipe.getRouter().isRoutedExit(EnumFacing.getFront(i))) {
					dirty = true;
				}
				isRoutedInDir[i] = cPipe.getRouter().isRoutedExit(EnumFacing.getFront(i));
			}
			for (int i = 0; i < 6; i++) {
				if (isSubPowerInDir[i] != cPipe.getRouter().isSubPoweredExit(EnumFacing.getFront(i))) {
					dirty = true;
				}
				isSubPowerInDir[i] = cPipe.getRouter().isSubPoweredExit(EnumFacing.getFront(i));
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
		if (dir == null) {
			return false;
		}
		return isRoutedInDir[dir.ordinal()];
	}

	public boolean isSubPowerInDir(EnumFacing dir) {
		if (dir == null) {
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

	public void writeData(LPDataOutput output) {
		for (int iconIndexe : iconIndexes) {
			output.writeByte(iconIndexe);
		}
		output.writeInt(textureIndex);
		output.writeBoolean(isRouted);
		output.writeBooleanArray(isRoutedInDir);
		output.writeBooleanArray(isSubPowerInDir);
		output.writeBoolean(hasPowerUpgrade);
		output.writeBoolean(hasPower);
		output.writeBoolean(isFluid);
		output.writeFacing(pointedOrientation);
	}

	public void readData(LPDataInput input) {
		for (int i = 0; i < iconIndexes.length; i++) {
			int icon = input.readByte();
			if (iconIndexes[i] != icon) {
				iconIndexes[i] = icon;
				dirty = true;
			}
		}
		textureIndex = input.readInt();
		isRouted = input.readBoolean();
		isRoutedInDir = input.readBooleanArray();
		isSubPowerInDir = input.readBooleanArray();
		hasPowerUpgrade = input.readBoolean();
		hasPower = input.readBoolean();
		isFluid = input.readBoolean();
		pointedOrientation = input.readFacing();
	}
}
