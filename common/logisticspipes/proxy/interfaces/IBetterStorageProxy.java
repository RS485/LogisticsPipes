package logisticspipes.proxy.interfaces;

import logisticspipes.proxy.bs.ICrateStorageProxy;

import net.minecraft.tileentity.TileEntity;

public interface IBetterStorageProxy {

	public boolean isBetterStorageCrate(TileEntity tile);

	public ICrateStorageProxy getCrateStorageProxy(TileEntity tile);
}
