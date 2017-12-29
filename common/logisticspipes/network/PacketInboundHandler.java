package logisticspipes.network;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler.InboundModernPacketWrapper;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.ticks.LPTickHandler;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.Level;

public class PacketInboundHandler extends SimpleChannelInboundHandler<InboundModernPacketWrapper> {

	private Map<Side, Map<Integer, Queue<InboundModernPacketWrapper>>> map;

	public PacketInboundHandler() {
		Map<Side, Map<Integer, Queue<InboundModernPacketWrapper>>> packetMap = Maps.newHashMap();
		for (Side side : Side.values()) {
			packetMap.put(side, new ConcurrentHashMap<>());
		}
		map = ImmutableMap.copyOf(packetMap);
		LPTickHandler.registerPacketHandler(this);
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InboundModernPacketWrapper msg) {
		Side side = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
		if (side != null) {
			int dimId = msg.packet.getDimension();
			if(side == Side.CLIENT) dimId = 0;
			Queue<InboundModernPacketWrapper> queue = getQueue(side, dimId);
			if(queue != null) {
				queue.add(msg);
				return;
			}
		}
		inThreadProcessPacket(msg.packet, msg.player);
	}

	public void tick(World world) {
		Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
		int dimId = world.provider.getDimension();
		if(side == Side.CLIENT) dimId = 0;
		Queue<InboundModernPacketWrapper> queue = getQueue(side, dimId);
		InboundModernPacketWrapper wrapper;
		while ((wrapper = queue.poll()) != null) {
			inThreadProcessPacket(wrapper.packet, wrapper.player);
		}
	}

	private void inThreadProcessPacket(ModernPacket packet, EntityPlayer player) {
		try {
			packet.processPacket(player);
			if (LPConstants.DEBUG) {
				PacketHandler.debugMap.remove(packet.getDebugId());
			}
		} catch (TargetNotFoundException e) {
			if (packet.retry() && MainProxy.isClient(player.getEntityWorld())) {
				SimpleServiceLocator.clientBufferHandler.queueFailedPacket(packet, player);
			} else if (LPConstants.DEBUG) {
				LogisticsPipes.log.error(packet.getClass().getName());
				LogisticsPipes.log.error(packet.toString());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		FMLLog.log(Level.ERROR, cause, "PacketInboundHandler exception");
		super.exceptionCaught(ctx, cause);
	}

	private Queue<InboundModernPacketWrapper> getQueue(Side side, int dimId) {
		Map<Integer, Queue<InboundModernPacketWrapper>> localMap = map.get(side);
		if (!localMap.containsKey(dimId)) {
			localMap.put(dimId, Queues.newConcurrentLinkedQueue());
		}
		return localMap.get(dimId);
	}
}
