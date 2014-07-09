package logisticspipes.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.NetworkRegistry;

public class PacketInboundHandler extends SimpleChannelInboundHandler<ModernPacket> {
    public PacketInboundHandler()
    {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ModernPacket msg) throws Exception
    {
        INetHandler iNetHandler = ctx.attr(NetworkRegistry.NET_HANDLER).get();
        EntityPlayer player = MainProxy.proxy.getEntityPlayerFromNetHandler(iNetHandler);
        msg.processPacket(player);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log(Level.ERROR, cause, "PacketInboundHandler exception");
        super.exceptionCaught(ctx, cause);
    }
}
