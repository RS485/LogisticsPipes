package logisticspipes.entity;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
import java.net.SocketAddress;
import java.util.Set;


public class FakeNetServerHandler extends NetHandlerPlayServer {

    public static class NetworkManagerFake extends NetworkManager {

        public NetworkManagerFake() {
            super(EnumPacketDirection.CLIENTBOUND);
        }

        @Override
        public void channelActive(ChannelHandlerContext p_channelActive_1_) { }

        @Override
        public void setConnectionState(@Nonnull EnumConnectionState newState) { }

        @Override
        public void channelInactive(ChannelHandlerContext p_channelInactive_1_) { }

        @Override
        public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, @Nonnull Throwable p_exceptionCaught_2_) { }

        @Override
        public void setNetHandler(INetHandler handler) { }

        @Override
        public void sendPacket(@Nonnull Packet<?> packetIn) { }

        @Override
        public void sendPacket(@Nonnull Packet<?> packetIn, @Nonnull GenericFutureListener<? extends Future<? super Void>> listener, @Nonnull GenericFutureListener<? extends Future<? super Void>>... listeners) { }

        @Override
        public void processReceivedPackets() { }

        @Nonnull
		@Override
        public SocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public boolean isLocalChannel() {
            return false;
        }

        @Override
        public void enableEncryption(SecretKey key) { }

        @Override
        public boolean isChannelOpen() {
            return false;
        }

        @Nonnull
		@Override
        public INetHandler getNetHandler() {
            return null;
        }

        @Nonnull
		@Override
        public ITextComponent getExitMessage() {
            return null;
        }

        @Override
        public void setCompressionThreshold(int threshold) { }

        @Override
        public void disableAutoRead() { }

        @Override
        public void checkDisconnected() { }

        @Nonnull
		@Override
        public Channel channel() {
            return null;
        }

    }

    public FakeNetServerHandler(MinecraftServer server, EntityPlayerMP playerIn) {
        super(server, new NetworkManagerFake(), playerIn);
    }

    @Override
    public void update() { }

    @Override
    public void disconnect(@Nonnull final ITextComponent textComponent) { }

    @Override
    public void processInput(CPacketInput packetIn) { }

    @Override
    public void processVehicleMove(CPacketVehicleMove packetIn) { }

    @Override
    public void processConfirmTeleport(CPacketConfirmTeleport packetIn) { }

    @Override
    public void processPlayer(CPacketPlayer packetIn) { }

    @Override
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) { }

    @Override
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPacketPlayerPosLook.EnumFlags> relativeSet) { }

    @Override
    public void processPlayerDigging(CPacketPlayerDigging packetIn) { }

    @Override
    public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn) { }

    @Override
    public void processTryUseItem(CPacketPlayerTryUseItem packetIn) { }

    @Override
    public void handleSpectate(@Nonnull CPacketSpectate packetIn) { }

    @Override
    public void handleResourcePackStatus(CPacketResourcePackStatus packetIn) { }

    @Override
    public void processSteerBoat(@Nonnull CPacketSteerBoat packetIn) { }

    @Override
    public void onDisconnect(ITextComponent reason) { }

    @Override
    public void sendPacket(@Nonnull final Packet<?> packetIn) { }

    @Override
    public void processHeldItemChange(CPacketHeldItemChange packetIn) { }

    @Override
    public void processChatMessage(@Nonnull CPacketChatMessage packetIn) { }

    @Override
    public void handleAnimation(CPacketAnimation packetIn) { }

    @Override
    public void processEntityAction(CPacketEntityAction packetIn) { }

    @Override
    public void processUseEntity(CPacketUseEntity packetIn) { }

    @Override
    public void processClientStatus(CPacketClientStatus packetIn) { }

    @Override
    public void processCloseWindow(@Nonnull CPacketCloseWindow packetIn) { }

    @Override
    public void processClickWindow(CPacketClickWindow packetIn) { }

    @Override
    public void processEnchantItem(CPacketEnchantItem packetIn) { }

    @Override
    public void processCreativeInventoryAction(@Nonnull CPacketCreativeInventoryAction packetIn) { }

    @Override
    public void processConfirmTransaction(@Nonnull CPacketConfirmTransaction packetIn) { }

    @Override
    public void processUpdateSign(CPacketUpdateSign packetIn) { }

    @Override
    public void processKeepAlive(@Nonnull CPacketKeepAlive packetIn) { }

    @Override
    public void processPlayerAbilities(CPacketPlayerAbilities packetIn) { }

    @Override
    public void processTabComplete(CPacketTabComplete packetIn) { }

    @Override
    public void processClientSettings(@Nonnull CPacketClientSettings packetIn) { }

    @Override
    public void processCustomPayload(CPacketCustomPayload packetIn) { }
}
