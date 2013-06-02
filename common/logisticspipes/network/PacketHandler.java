package logisticspipes.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.network.packets.abstracts.ModernPacket;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	public static List<ModernPacket> packetlist;

	public static Map<Class<? extends ModernPacket>, ModernPacket> packetmap;

	@SuppressWarnings("unchecked")
	public static <T extends ModernPacket> T getPacket(Class<T> clazz) {
		return (T)packetmap.get(clazz).template();
	}

	@SuppressWarnings("unchecked")
	public PacketHandler() {
		try {
			ImmutableSet<ClassInfo> classes = ClassPath.from(
					this.getClass().getClassLoader()).getTopLevelClasses(
					"logisticspipes.network.packets");

			packetlist = new ArrayList<ModernPacket>(classes.size());
			packetmap = new HashMap<Class<? extends ModernPacket>, ModernPacket>(
					classes.size());

			int currentid = 200;// TODO: Only 200 until all packets get
								// converted
			System.out.println("Loading " + classes.size() + "Packets");

			for (ClassInfo c : classes) {

				Class<?> cls = c.load();
				ModernPacket instance = (ModernPacket) cls
						.getConstructors()[0].newInstance(currentid++);
				packetlist.add(instance);
				packetmap.put((Class<? extends ModernPacket>) cls, instance);
			}
			Collections.sort(packetlist);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.data == null) {
			new Exception("Packet content has been null").printStackTrace();
		}
		if (MainProxy.isClient(((EntityPlayer) player).worldObj)) {
			ClientPacketHandler.onPacketData(manager, packet, player);
		} else {
			ServerPacketHandler.onPacketData(manager, packet, player);
		}
	}
}
