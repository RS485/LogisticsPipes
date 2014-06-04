package logisticspipes.ticks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import logisticspipes.proxy.MainProxy;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.tuples.Pair;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class QueuedTasks {
	
	@SuppressWarnings("rawtypes")
	private static LinkedList<Callable> queue = new LinkedList<Callable>();
	
	// called on server shutdown only.
	public static void clearAllTasks() {
		queue.clear();
	}
	
	@SuppressWarnings("rawtypes")
	public static void queueTask(Callable task) {
		synchronized (queue) {
			queue.add(task);
		}
	}
	
	@SuppressWarnings({"rawtypes" })
	@SubscribeEvent
	public void tickEnd(ServerTickEvent event) {
		if(event.phase != Phase.END) return;
		Callable call = null;
		while(!queue.isEmpty()) {
			synchronized (queue) {
				call = queue.removeFirst();
			}
			if(call != null) {
				try {
					call.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		MainProxy.proxy.tick();
		synchronized(LPTravelingItem.forceKeep) {
			Iterator<Pair<Integer, Object>> iter = LPTravelingItem.forceKeep.iterator();
			while(iter.hasNext()) {
				Pair<Integer, Object> pair = iter.next();
				pair.setValue1(pair.getValue1() - 1);
				if(pair.getValue1() < 0) {
					iter.remove();
				}
			}
		}
	}
}
