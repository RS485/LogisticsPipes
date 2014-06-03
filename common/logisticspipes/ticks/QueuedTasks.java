package logisticspipes.ticks;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import logisticspipes.proxy.MainProxy;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.tuples.Pair;

public class QueuedTasks implements ITickHandler {
	
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
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@SuppressWarnings({"rawtypes" })
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
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

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "LogisticsPipes QueuedTask";
	}

}
