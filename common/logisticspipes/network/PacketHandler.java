package logisticspipes.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.SneakyThrows;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	public static List<ModernPacket> packetlist;

	public static Map<Class<? extends ModernPacket>, ModernPacket> packetmap;

	@SuppressWarnings("unchecked")
	// Suppressed because this cast should never fail.
	public static <T extends ModernPacket> T getPacket(Class<T> clazz) {
		return (T) packetmap.get(clazz).template();
	}

	@SuppressWarnings("unchecked")
	// Suppressed because there shouldn't be non packet classes in the packets
	// directory.
	public PacketHandler() {
		try {

			final List<ClassInfo> classes = new ArrayList<ClassInfo>(ClassPath
					.from(this.getClass().getClassLoader())
					.getTopLevelClassesRecursive(
							"logisticspipes.network.packets"));
			Collections.sort(classes, new Comparator<ClassInfo>() {
				@Override
				public int compare(ClassInfo o1, ClassInfo o2) {
					return o1.getSimpleName().compareTo(o2.getSimpleName());
				}
			});

			packetlist = new ArrayList<ModernPacket>(classes.size());
			packetmap = new HashMap<Class<? extends ModernPacket>, ModernPacket>(
					classes.size());

			int currentid = 200;// TODO: Only 200 until all packets get
								// converted
			System.out.println("Loading " + classes.size() + " Packets");

			for (ClassInfo c : classes) {
				final Class<?> cls = c.load();
				final ModernPacket instance = (ModernPacket) cls
						.getConstructors()[0].newInstance(currentid);
				packetlist.add(instance);
				packetmap.put((Class<? extends ModernPacket>) cls, instance);

				System.out.println("Packet: " + c.getSimpleName() + " loaded");
				currentid++;
			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SneakyThrows(IOException.class)
	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.data == null) {
			new Exception("Packet content has been null").printStackTrace();
		}
		final DataInputStream data = new DataInputStream(
				new ByteArrayInputStream(packet.data));
		onPacketData(data, player);
	}

	public static void onPacketData(final DataInputStream data,
			final Player player) throws IOException {
		final int packetID = data.read();
		if (packetID >= 200) {// TODO: Temporary until all packets get converted
			final ModernPacket packet = PacketHandler.packetlist.get(
					packetID - 200).template();
			packet.readData(data);
			packet.processPacket((EntityPlayer) player);
		} else if (MainProxy.isClient(((EntityPlayer) player).worldObj)) {
			ClientPacketHandler.onPacketData(data, player, packetID);
		} else {
			ServerPacketHandler.onPacketData(data, player, packetID);
		}
	}
}
