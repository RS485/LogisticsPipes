package logisticspipes.main;

import java.util.HashMap;

import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsHUDRenderer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

public class LogisticsWorldManager {
	
	public static HashMap<Integer, Long> WorldLoadTime = new HashMap<Integer, Long>();
	
	@ForgeSubscribe
	public void WorldLoad(WorldEvent.Load event) {
		if(MainProxy.isServer(event.world)) {
			int dim = MainProxy.getDimensionForWorld(event.world);
			if(!WorldLoadTime.containsKey(dim)) {
				WorldLoadTime.put(dim, System.currentTimeMillis());
			}
		}
		if(MainProxy.isClient(event.world)) {
			SimpleServiceLocator.routerManager.clearClientRouters();
			LogisticsHUDRenderer.instance().clear();
		}
	}
}
