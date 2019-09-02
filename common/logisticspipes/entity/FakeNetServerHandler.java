package logisticspipes.entity;

import java.net.SocketAddress;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.crypto.SecretKey;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

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
