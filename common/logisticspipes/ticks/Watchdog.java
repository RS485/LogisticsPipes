package logisticspipes.ticks;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.utils.ObfuscationHelper;
import logisticspipes.utils.ObfuscationHelper.NAMES;
import net.minecraft.server.integrated.IntegratedServer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
// Based on
// https://raw.github.com/MinecraftPortCentral/MCPC-Plus/1acfe8e4d668b3fbc91b8d835451c5c56c74e7db/src/minecraft/org/spigotmc/WatchdogThread.java
public class Watchdog extends Thread {
	private static long timeStempServer = 0;
	private static long timeStempClient = 0;
	private final boolean isClient;
	private Field isGamePaused = null;
	
	public Watchdog(boolean isClient) {
		super("LP Watchdog");
		this.setDaemon(true);
		this.start();
		this.isClient = isClient;
		if(isClient) {
			try {
				isGamePaused = ObfuscationHelper.getDeclaredField(NAMES.isGamePausedServer, IntegratedServer.class);
				isGamePaused.setAccessible(true);
			} catch(NoSuchFieldException e) {
				e.printStackTrace();
			} catch(SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public static void tickServer() {
		timeStempServer = System.currentTimeMillis();
	}
	
	public static void tickClient() {
		timeStempClient = System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		while(true) {
			boolean server = false;
			boolean client = false;
			if(isClient) {
				boolean serverPaused = false;
				if(FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
					if(FMLCommonHandler.instance().getMinecraftServerInstance() instanceof IntegratedServer) {
						try {
							serverPaused = isGamePaused.getBoolean(FMLCommonHandler.instance().getMinecraftServerInstance());
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
						} catch(IllegalAccessException e) {
							e.printStackTrace();
						}
					}
					if(FMLCommonHandler.instance().getMinecraftServerInstance().isServerStopped()) {
						timeStempServer = 0;
					}
					server |= (timeStempServer + Configs.WATCHDOG_TIMEOUT < System.currentTimeMillis() && timeStempServer != 0 && !serverPaused && FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning());
				} else {
					if(timeStempServer != 0) {
						timeStempServer = System.currentTimeMillis();
					}
				}
				if(!FMLClientHandler.instance().getClient().running) {
					timeStempClient = 0;
				}
				client |= timeStempClient + Configs.WATCHDOG_TIMEOUT < System.currentTimeMillis() && timeStempClient != 0;
			} else {
				if(FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning()) {
					server |= timeStempServer + Configs.WATCHDOG_TIMEOUT < System.currentTimeMillis() && timeStempServer != 0;
				} else {
					if(timeStempServer != 0) {
						timeStempServer = System.currentTimeMillis();
					}
				}
			}
 			if((server && Configs.WATCHDOG_SERVER) || (client && Configs.WATCHDOG_CLIENT)) {
 				dump(server, client, false);
				LogisticsPipes.WATCHDOG = false;
				break;
			}
			try {
				Thread.sleep(10000);
			} catch(Exception e) {}
		}
	}
	
	public static void dump(boolean server, boolean client, boolean dump) {
		Logger log = LogisticsPipes.log;
		if(server) log.log(Level.SEVERE, "The server has stopped responding!");
		if(client) log.log(Level.SEVERE, "The client has stopped responding!");
		if(!dump) {
			log.log(Level.SEVERE, "This doesn't have to be a crash.");
			log.log(Level.SEVERE, "But still, please report this to https://github.com/RS485/LogisticsPipes/issues");
			log.log(Level.SEVERE, "Be sure to include ALL relevant console errors and Minecraft crash reports");
		}
		log.log(Level.SEVERE, "LP version: " + LogisticsPipes.VERSION);
		log.log(Level.SEVERE, "Current Thread State:");
		ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
		for(ThreadInfo thread: threads) {
			log.log(Level.SEVERE, "------------------------------");
			log.log(Level.SEVERE, "Current Thread: " + thread.getThreadName());
			log.log(Level.SEVERE, "\tPID: " + thread.getThreadId()
					+ " | Suspended: " + thread.isSuspended()
					+ " | Native: " + thread.isInNative()
					+ " | State: " + thread.getThreadState());
			if(thread.getLockedMonitors().length != 0) {
				log.log(Level.SEVERE, "\tThread is waiting on monitor(s):");
				for(MonitorInfo monitor: thread.getLockedMonitors()) {
					log.log(Level.SEVERE, "\t\tLocked on:" + monitor.getLockedStackFrame());
				}
			}
			if(thread.getThreadState() == Thread.State.WAITING) {
				log.log(Level.SEVERE, "\tWAITING ON: " + thread.getLockInfo().toString());
			}
			log.log(Level.SEVERE, "\tStack:");
			StackTraceElement[] stack = thread.getStackTrace();
			for(int line = 0; line < stack.length; line++) {
				log.log(Level.SEVERE, "\t\t" + stack[line].toString());
			}
		}
		log.log(Level.SEVERE, "------------------------------");
	}
}
