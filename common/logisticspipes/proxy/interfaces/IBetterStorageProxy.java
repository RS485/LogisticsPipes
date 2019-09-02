package logisticspipes.proxy.interfaces;

import logisticspipes.proxy.bs.ICrateStorageProxy;

import net.minecraft.tileentity.TileEntity;

public interface IBetterStorageProxy {

	boolean isBetterStorageCrate(TileEntity tile);

	ICrateStorageProxy getCrateStorageProxy(TileEntity tile);
}
