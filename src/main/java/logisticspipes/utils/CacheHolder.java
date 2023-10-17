package logisticspipes.utils;

import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import logisticspipes.asm.te.LPTileEntityObject;

/**
 * Cache is cleared every 200 ticks when used on Routed Pipes.
 */
public class CacheHolder {

	public enum CacheTypes {
		/**
		 * <p>
		 * Cleared when pipes [every supported type] are added to the network.
		 * </p>
		 */
		Routing,
		/**
		 * <p>
		 * Cleared when an item is inserted or extracted from an adjacent
		 * inventory<br>
		 * The Extraction trigger needs to be implemented separately for every
		 * use case.
		 * </p>
		 */
		Inventory
	}

	private final Table<CacheTypes, Object, Object> cache = HashBasedTable.create();

	public Object getCacheFor(CacheTypes type, Object key) {
		return cache.get(type, key);
	}

	public void setCache(CacheTypes type, Object key, Object value) {
		cache.put(type, key, value);
	}

	public void trigger(CacheTypes type) {
		if (type != null) {
			cache.row(type).clear();
		} else {
			cache.clear();
		}
	}

	public static void clearCache(Set<LPTileEntityObject> toClear) {
		for (LPTileEntityObject obj : toClear) {
			obj.trigger(CacheTypes.Routing);
		}
	}
}
