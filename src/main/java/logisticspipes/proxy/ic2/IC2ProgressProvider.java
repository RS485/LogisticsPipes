package logisticspipes.proxy.ic2;

import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;

import ic2.api.classic.tile.machine.IProgressMachine;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.interfaces.IGenericProgressProvider;

public class IC2ProgressProvider implements IGenericProgressProvider {

	@Nullable
	private Class<?> progressProviderClass = null;
	@Nullable
	private Function<TileEntity, Byte> getProgressFunction = null;

	public IC2ProgressProvider() {
		try {
			// support for IC2C progress
			progressProviderClass = IProgressMachine.class;
			getProgressFunction = this::getIC2CProgress;
			return;
		} catch (LinkageError error) {
			if (LogisticsPipes.isDEBUG()) {
				error.printStackTrace();
			}
		}
		try {
			progressProviderClass = TileEntityStandardMachine.class;
			getProgressFunction = this::getIC2Progress;
		} catch (LinkageError error) {
			if (LogisticsPipes.isDEBUG()) {
				error.printStackTrace();
			}
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		if (progressProviderClass == null) return false;
		return progressProviderClass.isInstance(tile);
	}

	private byte getIC2CProgress(TileEntity tile) {
		IProgressMachine progressMachine = (IProgressMachine) tile;
		float normalizedProgress = progressMachine.getProgress() / progressMachine.getMaxProgress();
		return (byte) Math.max(0, Math.min(normalizedProgress * 100, 100));
	}

	private byte getIC2Progress(TileEntity tile) {
		TileEntityStandardMachine standardMachine = (TileEntityStandardMachine) tile;
		return (byte) Math.max(0, Math.min(standardMachine.getProgress() * 100, 100));
	}

	@Override
	public byte getProgress(TileEntity tile) {
		if (getProgressFunction == null) return 0;
		return getProgressFunction.apply(tile);
	}
}
