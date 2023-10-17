package logisticspipes.ticks;

import java.util.concurrent.PriorityBlockingQueue;

import logisticspipes.config.Configs;

public class RoutingTableUpdateThread extends Thread {

	private static PriorityBlockingQueue<Runnable> updateCalls = new PriorityBlockingQueue<>();

	private static Long average = 0L;

	public RoutingTableUpdateThread(int i) {
		super("LogisticsPipes RoutingTableUpdateThread #" + i);
		setDaemon(true);
		setPriority(Configs.MULTI_THREAD_PRIORITY);
		start();
	}

	public static void add(Runnable run) {
		RoutingTableUpdateThread.updateCalls.add(run);
	}

	public static boolean remove(Runnable run) {
		return RoutingTableUpdateThread.updateCalls.remove(run);
	}

	public static int size() {
		return RoutingTableUpdateThread.updateCalls.size();
	}

	public static long getAverage() {
		synchronized (RoutingTableUpdateThread.average) {
			return RoutingTableUpdateThread.average;
		}
	}

	@Override
	public void run() {
		Runnable item;
		// take blocks until things are available, no need to check
		try {
			while ((item = RoutingTableUpdateThread.updateCalls.take()) != null) {
				long starttime = System.nanoTime();
				item.run();
				long took = System.nanoTime() - starttime;
				synchronized (RoutingTableUpdateThread.average) {
					if (RoutingTableUpdateThread.average == 0) {
						RoutingTableUpdateThread.average = took;
					} else {
						RoutingTableUpdateThread.average = ((RoutingTableUpdateThread.average * 999L) + took) / 1000L;
					}
				}
			}
		} catch (InterruptedException ignored) {

		}
	}
}
