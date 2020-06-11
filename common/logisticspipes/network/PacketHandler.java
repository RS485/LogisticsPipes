package logisticspipes.network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import static io.netty.buffer.Unpooled.buffer;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.Level;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.exception.DelayPacketException;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.StaticResolverUtil;
import network.rs485.logisticspipes.util.LPDataIOWrapper;
import network.rs485.logisticspipes.util.LPDataInput;

/*
 *  Basically FML SimpleIndexedCodec, except with static registration of LP ModernPackets and short instead of byte discriminator
 */
@Sharable
public class PacketHandler extends MessageToMessageCodec<FMLProxyPacket, ModernPacket> {

	public static final Map<Integer, StackTraceElement[]> debugMap = new HashMap<>();
	//TODO correct to work with WeakReference (See FML original)
	protected static final AttributeKey<ThreadLocal<FMLProxyPacket>> INBOUNDPACKETTRACKER = AttributeKey.newInstance("lp:inboundpacket");
	public static List<ModernPacket> packetlist;
	public static Map<Class<? extends ModernPacket>, ModernPacket> packetmap;
	private static int packetDebugID = 1;

	@SuppressWarnings("unchecked") // Suppressed because this cast should never fail.
	public static <T extends ModernPacket> T getPacket(Class<T> clazz) {
		T packet = (T) PacketHandler.packetmap.get(clazz).template();
		if (LogisticsPipes.isDEBUG() && MainProxy.proxy.getSide().equals("Client")) {
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			synchronized (PacketHandler.debugMap) { //Unique id
				int id = PacketHandler.packetDebugID++;
				PacketHandler.debugMap.put(id, trace);
				packet.setDebugId(id);
			}
		}
		return packet;
	}

	/*
	 * enumerates all ModernPackets, sets their IDs and populate packetlist/packetmap
	 */
	public static void initialize() {
		Set<Class<? extends ModernPacket>> classes = StaticResolverUtil.findClassesByType(ModernPacket.class);

		loadPackets(classes);

		if (PacketHandler.packetlist.isEmpty()) {
			throw new RuntimeException("Cannot load Packet Classes");
		}
	}

	private static void loadPackets(Set<Class<? extends ModernPacket>> classesIn) {
		List<Class<? extends ModernPacket>> classes = classesIn.stream()
				.sorted(Comparator.comparing(Class::getCanonicalName))
				.collect(Collectors.toList());

		PacketHandler.packetlist = new ArrayList<>(classes.size());
		PacketHandler.packetmap = new HashMap<>(classes.size());

		int currentId = 0;
		for (Class<? extends ModernPacket> cls : classes) {
			try {
				final ModernPacket instance = cls.getConstructor(int.class).newInstance(currentId);
				PacketHandler.packetlist.add(instance);
				PacketHandler.packetmap.put(cls, instance);
				currentId++;
			} catch (Throwable ignoredButPrinted) {
				ignoredButPrinted.printStackTrace();
			}
		}
	}

	//Used to provide the Description packet
	public static FMLProxyPacket toFMLPacket(ModernPacket msg) throws Exception {
		return PacketHandler.toFMLPacket(msg, MainProxy.networkChannelName);
	}

	private static FMLProxyPacket toFMLPacket(ModernPacket msg, String channel) {
		ByteBuf buffer = Unpooled.buffer();
		fillByteBuf(msg, buffer);

		return new FMLProxyPacket(new PacketBuffer(buffer), channel);
	}

	public static void fillByteBuf(ModernPacket msg, ByteBuf buffer) {
		buffer.writeShort(msg.getId());
		buffer.writeInt(msg.getDebugId());

		LPDataIOWrapper.writeData(buffer, msg::writeData);
	}

	public static void addPacketToNBT(ModernPacket packet, NBTTagCompound nbt) {
		ByteBuf dataBuffer = buffer();
		PacketHandler.fillByteBuf(packet, dataBuffer);

		byte[] data = new byte[dataBuffer.readableBytes()];
		dataBuffer.getBytes(0, data);
		dataBuffer.release();

		nbt.setByteArray("LogisticsPipes:PacketData", data);
	}

	@SideOnly(Side.CLIENT)
	public static void queueAndRemovePacketFromNBT(NBTTagCompound nbt) {
		byte[] data = nbt.getByteArray("LogisticsPipes:PacketData");
		if (data.length > 0) {
			LPDataIOWrapper.provideData(data, dataInput -> {
				final int packetID = dataInput.readShort();
				final ModernPacket packet = PacketHandler.packetlist.get(packetID).template();
				packet.setDebugId(dataInput.readInt());
				packet.readData(dataInput);
				SimpleServiceLocator.clientBufferHandler.queuePacket(packet, MainProxy.proxy.getClientPlayer());
			});
		}
		nbt.removeTag("LogisticsPipes:PacketData");
	}

	//hacky callback to process packets coming from by the packetbufferhandler decompressors
	//TODO replace with proper netty implementation
	public static void onPacketData(final LPDataInput data, final EntityPlayer player) {
		if (player == null) {
			return;
		}
		final int packetID = data.readShort();
		final ModernPacket packet = PacketHandler.packetlist.get(packetID).template();
		packet.setDebugId(data.readInt());
		packet.readData(data);
		PacketHandler.onPacketData(packet, player);
	}

	public static void onPacketData(ModernPacket packet, final EntityPlayer player) {
		try {
			packet.processPacket(player);
			if (LogisticsPipes.isDEBUG()) {
				PacketHandler.debugMap.remove(packet.getDebugId());
			}
		} catch (DelayPacketException e) {
			if (packet.retry() && MainProxy.isClient(player.getEntityWorld())) {
				SimpleServiceLocator.clientBufferHandler.queuePacket(packet, player);
			} else if (LogisticsPipes.isDEBUG()) {
				LogisticsPipes.log.error(packet.getClass().getName());
				LogisticsPipes.log.error(packet.toString());
				e.printStackTrace();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		ctx.attr(PacketHandler.INBOUNDPACKETTRACKER).set(new ThreadLocal<>());
	}

	@Override
	protected final void encode(ChannelHandlerContext ctx, ModernPacket msg, List<Object> out) throws Exception {
		FMLProxyPacket proxy = PacketHandler.toFMLPacket(msg, ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
		FMLProxyPacket old = ctx.attr(PacketHandler.INBOUNDPACKETTRACKER).get().get();
		if (old != null) {
			proxy.setDispatcher(old.getDispatcher());
		}
		out.add(proxy);
	}

	@Override
	protected final void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) {
		ByteBuf payload = msg.payload();
		int packetID = payload.readShort();
		final ModernPacket packet = PacketHandler.packetlist.get(packetID).template();
		packet.setDebugId(payload.readInt());
		ctx.attr(PacketHandler.INBOUNDPACKETTRACKER).get().set(msg);

		LPDataIOWrapper.provideData(payload.slice(), packet::readData);

		EntityPlayer player = MainProxy.proxy.getEntityPlayerFromNetHandler(msg.handler());

		if (player != null) {
			out.add(new InboundModernPacketWrapper(packet, player));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		FMLLog.log(Level.ERROR, cause, "LogisticsPipes PacketHandler exception caught");
		super.exceptionCaught(ctx, cause);
	}

	//horrible hack to carry the proper player for the side along...
	static class InboundModernPacketWrapper {

		final ModernPacket packet;
		final EntityPlayer player;

		InboundModernPacketWrapper(ModernPacket p, EntityPlayer e) {
			packet = p;
			player = e;
		}
	}
}
