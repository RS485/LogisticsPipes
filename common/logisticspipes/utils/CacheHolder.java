package logisticspipes.utils;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.asm.te.LPTileEntityObject;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Cache is cleared every 200 ticks when used on Routed Pipes.
 */
public class CacheHolder {
	public static enum CacheTypes {
		/** 
		 * <p>
		 * Cleared when pipes [every supported type] are added to the network.
		 * </p>
		 */
		Routing, 
		/**
		 * <p>
		 * Cleared when an item is inserted or extracted from an adjacent inventory<br>
		 * The Extraction trigger needs to be implemented separately for every use case.
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
		if(type != null) {
			cache.row(type).clear();
		} else {
			cache.clear();
		}
	}

	private static CacheThread thread;
	
	public static void clearCache(List<LPTileEntityObject> toClear) {
		if(thread == null || !thread.isAlive()) {
			thread = new CacheThread(toClear);
		} else {
			thread.cachesToClear.addAll(toClear);
		}
	}
	
	private static class CacheThread extends Thread {
		private List<LPTileEntityObject> cachesToClear;
		private CacheThread(List<LPTileEntityObject> list) {
			cachesToClear = new LinkedList<LPTileEntityObject>(list);
			this.setDaemon(true);
			this.start();
		}
		
		public void run() {
			while(thread == this) {
				if(!cachesToClear.isEmpty()) {
					cachesToClear.remove(0).trigger(CacheTypes.Routing);
				} else {
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {}
				}
			}
		}
	}
}
