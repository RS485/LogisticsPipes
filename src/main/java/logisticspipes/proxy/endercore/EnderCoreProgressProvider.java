package logisticspipes.proxy.endercore;

import net.minecraft.tileentity.TileEntity;

import com.enderio.core.api.common.util.IProgressTile;

import logisticspipes.proxy.interfaces.IGenericProgressProvider;

public class EnderCoreProgressProvider implements IGenericProgressProvider {

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof IProgressTile;
	}

	@Override
	public byte getProgress(TileEntity tile) {
		return (byte) Math.max(0, Math.min(((IProgressTile) tile).getProgress() * 100, 100));
	}
}
