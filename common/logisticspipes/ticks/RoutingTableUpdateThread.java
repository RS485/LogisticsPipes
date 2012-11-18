package logisticspipes.ticks;

import java.util.LinkedList;

public class RoutingTableUpdateThread extends Thread {
	
	private static LinkedList<Runnable> updateCalls = new LinkedList<Runnable>();
	
	public RoutingTableUpdateThread(int i) {
		super("LogisticsPipes RoutingTableUpdateThread #" + i);
		this.setDaemon(true);
		//this.setPriority(MAX_PRIORITY); //TODO Config
		this.start();
	}

	public static void add(Runnable run) {
		synchronized (updateCalls) {
			updateCalls.add(run);
		}
	}
	
	public static void addPriority(Runnable run) {
		synchronized (updateCalls) {
			updateCalls.addFirst(run);
		}
	}

	public static boolean remove(Runnable run) {
		synchronized (updateCalls) {
			return updateCalls.remove(run);
		}
	}

	public static int size() {
		synchronized (updateCalls) {
			return updateCalls.size();
		}
	}
	
	@Override
	public void run() {
		while(true) {
			boolean doUpdate = false;
			synchronized (updateCalls) {
				if(updateCalls.size() > 0) {
					doUpdate = true;
				}
			}
			if(doUpdate) {
				boolean run = true;
				while(run) {
					Runnable localCall = null;
					synchronized (updateCalls) {
						if(updateCalls.isEmpty()) {
							run = false;
						} else {
							localCall = updateCalls.getFirst();
							updateCalls.removeFirst();
						}
					}
					if(run) {
						localCall.run();
					}
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
