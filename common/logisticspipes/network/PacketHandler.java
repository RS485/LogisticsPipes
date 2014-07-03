package logisticspipes.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.SneakyThrows;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class PacketHandler extends MessageToMessageCodec<FMLProxyPacket, ModernPacket> {

	public static List<ModernPacket> packetlist;

	public static Map<Class<? extends ModernPacket>, ModernPacket> packetmap;

	@SuppressWarnings("unchecked")
	// Suppressed because this cast should never fail.
	public static <T extends ModernPacket> T getPacket(Class<T> clazz) {
		return (T) packetmap.get(clazz).template();
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows({ IOException.class, InvocationTargetException.class, IllegalAccessException.class, InstantiationException.class, NoSuchFieldException.class, SecurityException.class })
	// Suppression+sneakiness because these shouldn't ever fail, and if they do, it needs to fail.
	public static final void intialize() {
		Field FINBOUNDPACKETTRACKER = FMLIndexedMessageToMessageCodec.class.getDeclaredField("INBOUNDPACKETTRACKER");
		FINBOUNDPACKETTRACKER.setAccessible(true);
		INBOUNDPACKETTRACKER = (AttributeKey<ThreadLocal<FMLProxyPacket>>) FINBOUNDPACKETTRACKER.get(null);
		
		final List<ClassInfo> classes = new ArrayList<ClassInfo>(ClassPath.from(PacketHandler.class.getClassLoader()).getTopLevelClassesRecursive("logisticspipes.network.packets"));
		Collections.sort(classes, new Comparator<ClassInfo>() {
			@Override
			public int compare(ClassInfo o1, ClassInfo o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});

		packetlist = new ArrayList<ModernPacket>(classes.size());
		packetmap = new HashMap<Class<? extends ModernPacket>, ModernPacket>(classes.size());

		int currentid = 0;

		for (ClassInfo c : classes) {
			final Class<?> cls = c.load();
			final ModernPacket instance = (ModernPacket) cls.getConstructors()[0].newInstance(currentid);
			packetlist.add(instance);
			packetmap.put((Class<? extends ModernPacket>) cls, instance);
			currentid++;
		}
	}
	
	protected static AttributeKey<ThreadLocal<FMLProxyPacket>>	INBOUNDPACKETTRACKER;
	
	@Override
	protected void encode(ChannelHandlerContext ctx, ModernPacket msg, List<Object> out) throws Exception {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeInt(msg.getId());
		msg.writeData(new LPDataOutputStream(buffer));
		FMLProxyPacket proxy = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
		FMLProxyPacket old = ctx.attr(INBOUNDPACKETTRACKER).get().get();
		if(old != null) {
			proxy.setDispatcher(old.getDispatcher());
		}
		out.add(proxy);
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
		INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
		EntityPlayer player = MainProxy.proxy.getEntityPlayerFromNetHandler(netHandler);
		ByteBuf payload = msg.payload();
		int packetID = payload.readInt();
		final ModernPacket packet = PacketHandler.packetlist.get(packetID).template();
		packet.readData(new LPDataInputStream(payload));
		try {
			packet.processPacket(player);
		} catch(Exception e) {
			LogisticsPipes.log.error(packet.getClass().getName());
			LogisticsPipes.log.error(packet.toString());
			throw new RuntimeException(e);
		}
		ctx.attr(INBOUNDPACKETTRACKER).get().set(msg);
		out.add(packet);
	}
	
	public static void onPacketData(LPDataInputStream s, EntityPlayer p) {
		try {
			int packetID = s.readInt();
			final ModernPacket packet = PacketHandler.packetlist.get(packetID).template();
			packet.readData(s);
			packet.processPacket(p);
		} catch(Exception e) {
			//LogisticsPipes.log.error(packet.getClass().getName());
			//LogisticsPipes.log.error(packet.toString());
			throw new RuntimeException(e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		FMLLog.log(Level.ERROR, cause, "LogisticsPipes PacketHandler exception caught");
		super.exceptionCaught(ctx, cause);
	}
}
