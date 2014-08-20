package logisticspipes.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler.InboundModernPacketWrapper;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

public class PacketInboundHandler extends SimpleChannelInboundHandler<InboundModernPacketWrapper> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InboundModernPacketWrapper msg) throws Exception {
		try {
			msg.packet.processPacket(msg.player);
			if(LogisticsPipes.DEBUG) {
				PacketHandler.debugMap.remove((Integer) msg.packet.getDebugId());
			}
		} catch(TargetNotFoundException e) {
			if(msg.packet.retry() && MainProxy.isClient(msg.player.getEntityWorld())) {
				SimpleServiceLocator.clientBufferHandler.queueFailedPacket(msg.packet, msg.player);
			} else if(LogisticsPipes.DEBUG) {
				LogisticsPipes.log.error(msg.packet.getClass().getName());
				LogisticsPipes.log.error(msg.packet.toString());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		FMLLog.log(Level.ERROR, cause, "PacketInboundHandler exception");
		super.exceptionCaught(ctx, cause);
	}
}
