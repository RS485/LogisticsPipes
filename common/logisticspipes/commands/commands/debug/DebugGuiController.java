package logisticspipes.commands.commands.debug;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.exception.DelayPacketException;
import logisticspipes.network.packets.debuggui.DebugDataPacket;
import logisticspipes.network.packets.debuggui.DebugPanelOpen;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.AllArgsConstructor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import network.rs485.debuggui.api.IDataConnection;
import network.rs485.debuggui.api.IDebugGuiEntry;
import network.rs485.debuggui.api.IObjectIdentification;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DebugGuiController {

	static {
		Launch.classLoader.addTransformerExclusion("com.trolltech.qt.");
		Launch.classLoader.addTransformerExclusion("network.rs485.debuggui.");
	}

	transient private static DebugGuiController instance;

	private DebugGuiController() {}

	public static DebugGuiController instance() {
		if (DebugGuiController.instance == null) {
			DebugGuiController.instance = new DebugGuiController();
		}
		return DebugGuiController.instance;
	}

	public void execClient() {
		if(clientController != null) {
			clientController.exec();
		}
	}

	public void execServer() {
		for(IDebugGuiEntry entry:serverDebugger.values()) {
			entry.exec();
		}
	}

	private HashMap<EntityPlayer, IDebugGuiEntry> serverDebugger = new HashMap<EntityPlayer, IDebugGuiEntry>();
	private List<IDataConnection> serverList = new LinkedList<IDataConnection>();

	private IDebugGuiEntry clientController = null;
	private List<Future<IDataConnection>> clientList = new LinkedList<Future<IDataConnection>>();

	public void startWatchingOf(Object object, EntityPlayer player) {
		if (object == null) {
			return;
		}
		IDebugGuiEntry entry = serverDebugger.get(player);
		if(entry == null) {
			try {
				entry = IDebugGuiEntry.create();
				serverDebugger.put(player, entry);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return;
			} catch (InstantiationException e) {
				e.printStackTrace();
				return;
			}
		}
		if(entry == null) {
			System.out.println("DebugGui could not be loaded");
			return;
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugPanelOpen.class).setName(object.getClass().getSimpleName()), player);
		synchronized (serverList) {
			int identification = serverList.size();
			IDataConnection conIn = new DataConnectionServer(identification, player);
			while(serverList.size() <= identification) serverList.add(null);
			serverList.set(identification, entry.startServerDebugging(object, conIn, new ObjectIdentification()));
		}
	}

	public void createNewDebugGui(String name, int identification) {
		if(clientController == null) {
			try {
				clientController = IDebugGuiEntry.create();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return;
			} catch (InstantiationException e) {
				e.printStackTrace();
				return;
			}
		}
		synchronized (clientList) {
			while(clientList.size() <= identification) clientList.add(null);
			clientList.set(identification, clientController.startClientDebugging(name, new DataConnectionClient(identification)));
		}
	}

	public void handleDataPacket(byte[] payload, int identifier, EntityPlayer player) {
		if(MainProxy.isServer(player.getEntityWorld())) {
			synchronized (serverList) {
				IDataConnection connection = serverList.get(identifier);
				if(connection != null) {
					connection.passData(payload);
				}
			}
		} else {
			synchronized (clientList) {
				Future<IDataConnection> connectionFuture;
				try {
					connectionFuture = clientList.get(identifier);
				} catch(IndexOutOfBoundsException e) {
					System.out.println(clientList);
					throw e;
				}
				if(connectionFuture == null || !connectionFuture.isDone()) {
					throw new DelayPacketException();
				}
				IDataConnection connection = null;
				try {
					connection = connectionFuture.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				if(connection != null) {
					connection.passData(payload);
				} else {
					throw new DelayPacketException();
				}
			}
		}
	}

	@AllArgsConstructor
	private class DataConnectionServer implements IDataConnection {

		private int identification;
		private EntityPlayer player;

		@Override
		public void passData(byte[] packet) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugDataPacket.class).setPayload(packet).setIdentifier(identification), player);
		}

		@Override
		public void closeCon() {
			serverList.set(identification, null);
		}
	}

	@AllArgsConstructor
	private class DataConnectionClient implements IDataConnection {

		private int identification;

		@Override
		public void passData(byte[] packet) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugDataPacket.class).setPayload(packet).setIdentifier(identification));
		}

		@Override
		public void closeCon() {
			clientList.set(identification, null);
		}
	}

	private class ObjectIdentification implements IObjectIdentification {
		@Override
		public boolean toStringObject(Object o) {
			return o.getClass() == ForgeDirection.class || o.getClass() == ItemIdentifier.class || o.getClass() == ItemIdentifierStack.class;
		}

		@Override
		public String handleObject(Object o) {
			if(o instanceof World) {
				return ((World)o).getWorldInfo().getWorldName();
			}
			if(o != null && o.getClass().isArray() && Array.getLength(o) > 100) {
				return "(Too big)";
			}
			return null;
		}
	}
}
