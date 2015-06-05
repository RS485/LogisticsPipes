package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

public interface IGenericProgressProvider {

	public boolean isType(TileEntity tile);

	public byte getProgress(TileEntity tile);
}
