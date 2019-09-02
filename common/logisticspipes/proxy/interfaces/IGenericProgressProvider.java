package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

public interface IGenericProgressProvider {

	boolean isType(TileEntity tile);

	byte getProgress(TileEntity tile);
}
