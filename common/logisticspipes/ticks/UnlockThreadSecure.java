package logisticspipes.ticks;

public class UnlockThreadSecure extends Thread {
	
	public boolean running;
	private long stopTime;
	private Thread thread;
	
	public UnlockThreadSecure(int delay, Thread thread) {
		this.running = true;
		this.stopTime = System.currentTimeMillis() + delay;
		this.thread = thread;
		this.start();
	}
	
	public void run() {
		while(running) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(stopTime < System.currentTimeMillis()) {
				running = false;
				thread.resume();
			}
		}
	}
}
