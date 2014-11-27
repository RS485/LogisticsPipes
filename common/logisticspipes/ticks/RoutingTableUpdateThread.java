package logisticspipes.ticks;

import java.util.concurrent.PriorityBlockingQueue;

import logisticspipes.config.Configs;

public class RoutingTableUpdateThread extends Thread {
	
	private static PriorityBlockingQueue<Runnable> updateCalls = new PriorityBlockingQueue<Runnable>();
	
	private static Long average = 0L;

	public RoutingTableUpdateThread(int i) {
		super("LogisticsPipes RoutingTableUpdateThread #" + i);
		this.setDaemon(true);
		this.setPriority(Configs.MULTI_THREAD_PRIORITY);
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
	
	public static long getAverage() {
		synchronized(average) {
			return average;
		}
	}
	
	@Override
	public void run() {
		Runnable item = null;
		// take blocks until things are available, no need to check
		try {
			while((item = updateCalls.take()) != null) {
				long starttime = System.nanoTime();
				item.run();
				long took = System.nanoTime() - starttime;
				synchronized(average) {
					if(average == 0) {
						average = took;
					} else {
						average = ((average * 999L) + took) / 1000L;
					}
				}
			}
		} catch (InterruptedException e) {
			
		}
	}
}
