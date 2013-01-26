package logisticspipes.ticks;

import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;

import logisticspipes.config.Configs;
import logisticspipes.routing.IRouter;

public class RoutingTableUpdateThread extends Thread {
	
	private static PriorityBlockingQueue<Runnable> updateCalls = new PriorityBlockingQueue<Runnable>();

	public RoutingTableUpdateThread(int i) {
		super("LogisticsPipes RoutingTableUpdateThread #" + i);
		this.setDaemon(true);
		this.setPriority(Configs.multiThreadPriority);
		this.start();
	}

	public static void add(Runnable run) {
		updateCalls.add(run);
	}

	public static boolean remove(Runnable run) {
		return updateCalls.remove(run);
	}

	public static int size() {
		return updateCalls.size();
	}
	
	@Override
	public void run() {
		Runnable item = null;
		// take blocks until things are available, no need to check
		try {
			while((item = updateCalls.take()) != null) {
				item.run();
			}
		} catch (InterruptedException e) {
			
		}
	}
}
