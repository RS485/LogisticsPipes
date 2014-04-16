package logisticspipes.proxy.bs;

import logisticspipes.proxy.interfaces.IBetterStorageProxy;
import net.minecraft.tileentity.TileEntity;

public class BetterStorageProxy implements IBetterStorageProxy {
	
	private Class<?> crateClass;
	
	public BetterStorageProxy() throws ClassNotFoundException {
		crateClass = Class.forName("net.mcft.copy.betterstorage.tile.crate.TileEntityCrate");
	}
	@Override
	public boolean isBetterStorageCrate(TileEntity tile) {
		return crateClass.isAssignableFrom(tile.getClass());
	}
}
