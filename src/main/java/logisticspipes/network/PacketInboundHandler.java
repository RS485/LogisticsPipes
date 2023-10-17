package logisticspipes.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.Level;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler.InboundModernPacketWrapper;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;

public class PacketInboundHandler extends SimpleChannelInboundHandler<InboundModernPacketWrapper> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InboundModernPacketWrapper msg) {
		IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
		if (thread.isCallingFromMinecraftThread()) {
			inThreadProcessPacket(msg.packet, msg.player);
		} else {
			thread.addScheduledTask(() -> inThreadProcessPacket(msg.packet, msg.player));
		}
	}

	private void inThreadProcessPacket(ModernPacket packet, EntityPlayer player) {
		try {
			packet.processPacket(player);
			if (LogisticsPipes.isDEBUG()) {
				PacketHandler.debugMap.remove(packet.getDebugId());
			}
		} catch (TargetNotFoundException e) {
			if (packet.retry() && MainProxy.isClient(player.getEntityWorld())) {
				SimpleServiceLocator.clientBufferHandler.queuePacket(packet, player);
			} else if (LogisticsPipes.isDEBUG()) {
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
}
