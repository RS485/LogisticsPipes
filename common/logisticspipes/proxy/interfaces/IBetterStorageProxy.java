package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.proxy.bs.ICrateStorageProxy;

public interface IBetterStorageProxy {

	boolean isBetterStorageCrate(TileEntity tile);

	ICrateStorageProxy getCrateStorageProxy(TileEntity tile);
}
