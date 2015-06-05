package logisticspipes.asm.te;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.CacheHolder.CacheTypes;

public class LPTileEntityObject {

	public List<ITileEntityChangeListener> changeListeners = new ArrayList<ITileEntityChangeListener>();
	public long initialised = 0;

	private CacheHolder cacheHolder;

	public CacheHolder getCacheHolder() {
		if (cacheHolder == null) {
			cacheHolder = new CacheHolder();
		}
		return cacheHolder;
	}

	public void trigger(CacheTypes type) {
		if (cacheHolder != null) {
			getCacheHolder().trigger(type);
		}
	}
}
