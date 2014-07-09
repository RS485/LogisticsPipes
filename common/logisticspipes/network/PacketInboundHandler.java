package logisticspipes.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import logisticspipes.network.PacketHandler.InboundModernPacketWrapper;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;

public class PacketInboundHandler extends SimpleChannelInboundHandler<InboundModernPacketWrapper> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InboundModernPacketWrapper msg) throws Exception
	{
		msg.packet.processPacket(msg.player);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		FMLLog.log(Level.ERROR, cause, "PacketInboundHandler exception");
		super.exceptionCaught(ctx, cause);
	}
}
