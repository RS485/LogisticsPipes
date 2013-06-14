package logisticspipes.proxy.bs;

import logisticspipes.proxy.interfaces.IBetterStorageProxy;
import net.minecraft.tileentity.TileEntity;

public class BetterStorageProxy implements IBetterStorageProxy {
	
	private Class<?> crateClass;
	private boolean init = false;
	
	public BetterStorageProxy() {
		try {
			crateClass = Class.forName("net.mcft.copy.betterstorage.block.crate.TileEntityCrate");
			init = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean isBetterStorageCrate(TileEntity tile) {
		if(!init) return false;
		return crateClass.isAssignableFrom(tile.getClass());
	}
}
