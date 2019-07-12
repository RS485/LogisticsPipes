package logisticspipes.proxy.te;

import net.minecraft.tileentity.TileEntity;

import cofh.core.block.TileNameable;

import logisticspipes.proxy.interfaces.IGenericProgressProvider;

public class ThermalExpansionProgressProvider implements IGenericProgressProvider {

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof TileNameable;
	}

	@Override
	public byte getProgress(TileEntity tile) {
		return (byte) Math.max(0, Math.min(((TileNameable) tile).getScaledProgress(100), 100));
	}
}
