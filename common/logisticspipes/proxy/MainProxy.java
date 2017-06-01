package logisticspipes.proxy;

import java.io.File;
import java.util.EnumMap;
import java.util.WeakHashMap;

import logisticspipes.LogisticsEventListener;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.FakePlayer;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.PacketInboundHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.routing.debug.RoutingTableDebugUpdateThread;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.PlayerCollectionList;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

import lombok.Getter;

public class MainProxy {

	private MainProxy() {}

	@SidedProxy(clientSide = "logisticspipes.proxy.side.ClientProxy", serverSide = "logisticspipes.proxy.side.ServerProxy")
	public static IProxy proxy;
	@Getter
	private static int globalTick;
	public static EnumMap<Side, FMLEmbeddedChannel> channels;

	private static WeakHashMap<Thread, Side> threadSideMap = new WeakHashMap<Thread, Side>();
	public static final String networkChannelName = "LogisticsPipes";

	private static Side getEffectiveSide() {
		Thread thr = Thread.currentThread();
		if (MainProxy.threadSideMap.containsKey(thr)) {
			return MainProxy.threadSideMap.get(thr);
		}
		Side side = MainProxy.getEffectiveSide(thr);
		if (MainProxy.threadSideMap.size() > 50) {
			MainProxy.threadSideMap.clear();
		}
		MainProxy.threadSideMap.put(thr, side);
		return side;
	}

	private static Side getEffectiveSide(Thread thr) {
		if (thr.getName().equals("Server thread") || (thr instanceof RoutingTableUpdateThread) || (thr instanceof RoutingTableDebugUpdateThread)) {
			return Side.SERVER;
		}
		if (SimpleServiceLocator.ccProxy != null && SimpleServiceLocator.ccProxy.isLuaThread(thr)) {
			return Side.SERVER;
		}
		return Side.CLIENT;
	}

	public static boolean isClient(World world) {
		try {
			return world.isRemote;
		} catch (NullPointerException n) {
			LogisticsPipes.log.fatal("isClient called with a null world - using slow thread based fallback");
			n.printStackTrace();
		}
		return MainProxy.isClient();
	}

	@Deprecated
	/**
	 * isClient is slow, find a world and check isServer(world)
	 * @return
	 */
	public static boolean isClient() {
		return MainProxy.getEffectiveSide() == Side.CLIENT;
	}

	public static boolean isServer(World world) {
		try {
			return !world.isRemote;
		} catch (NullPointerException n) {
			LogisticsPipes.log.fatal("isServer called with a null world - using slow thread based fallback");
			n.printStackTrace();
		}
		return MainProxy.isServer();
	}

	@Deprecated
	/**
	 * isServer is slow, find a world and check isServer(world)
	 * @return
	 */
	public static boolean isServer() {
		return MainProxy.getEffectiveSide() == Side.SERVER;
	}

	public static World getClientMainWorld() {
		return MainProxy.proxy.getWorld();
	}

	public static int getDimensionForWorld(World world) {
		return MainProxy.proxy.getDimensionForWorld(world);
	}

	public static void createChannels() {
		MainProxy.channels = NetworkRegistry.INSTANCE.newChannel(MainProxy.networkChannelName, new PacketHandler());
		for (Side side : Side.values()) {
			FMLEmbeddedChannel channel = MainProxy.channels.get(side);
			String type = channel.findChannelHandlerNameForType(PacketHandler.class);
			channel.pipeline().addAfter(type, PacketInboundHandler.class.getName(), new PacketInboundHandler());
		}
	}

	public static void sendPacketToServer(ModernPacket packet) {
		if (MainProxy.isServer()) {
			System.err.println("sendPacketToServer called serverside !");
			new Exception().printStackTrace();
			return;
		}
		if (packet.isCompressable()) {
			SimpleServiceLocator.clientBufferHandler.addPacketToCompressor(packet);
		} else {
			MainProxy.channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
			MainProxy.channels.get(Side.CLIENT).writeOutbound(packet);
		}
	}

	public static void sendPacketToPlayer(ModernPacket packet, EntityPlayer player) {
		if (!MainProxy.isServer(player.worldObj)) {
			System.err.println("sendPacketToPlayer called clientside !");
			new Exception().printStackTrace();
			return;
		}
		if(player instanceof net.minecraftforge.common.util.FakePlayer) {
			return;
		}
		if (packet.isCompressable()) {
			SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet, player);
		} else {
			MainProxy.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
			MainProxy.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
			MainProxy.channels.get(Side.SERVER).writeOutbound(packet);
		}
	}

	// ignores dimension; more stringent check done inside sendPacketToAllWatching
	public static boolean isAnyoneWatching(int X, int Z, int dimensionID) {
		ChunkCoordIntPair chunk = new ChunkCoordIntPair(X >> 4, Z >> 4);
		PlayerCollectionList players = LogisticsEventListener.watcherList.get(chunk);
		if (players == null) {
			return false;
		}
		return !players.isEmptyWithoutCheck();
	}

	public static void sendPacketToAllWatchingChunk(int X, int Z, int dimensionId, ModernPacket packet) {
		if (!MainProxy.isServer()) {
			System.err.println("sendPacketToAllWatchingChunk called clientside !");
			new Exception().printStackTrace();
			return;
		}
		ChunkCoordIntPair chunk = new ChunkCoordIntPair(X >> 4, Z >> 4);
		PlayerCollectionList players = LogisticsEventListener.watcherList.get(chunk);
		if (players != null) {
			for (EntityPlayer player : players.players()) {
				if (MainProxy.getDimensionForWorld(player.worldObj) == dimensionId) {
					MainProxy.sendPacketToPlayer(packet, player);
				}
			}
			return;
		}
	}

	public static void sendToPlayerList(ModernPacket packet, PlayerCollectionList players) {
		if (players.isEmpty()) {
			return;
		}
		sendToPlayerList(packet, players.players());
	}

	public static void sendToPlayerList(ModernPacket packet, Iterable<EntityPlayer> players) {

		if (!MainProxy.isServer()) {
			System.err.println("sendToPlayerList called clientside !");
			new Exception().printStackTrace();
			return;
		}
		if (packet.isCompressable()) {
			for (EntityPlayer player : players) {
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet, player);
			}
		} else {
			for (EntityPlayer player : players) {
				MainProxy.sendPacketToPlayer(packet, player);
			}
		}
	}

	public static void sendToAllPlayers(ModernPacket packet) {
		if (!MainProxy.isServer()) {
			System.err.println("sendToAllPlayers called clientside !");
			new Exception().printStackTrace();
			return;
		}
		if (packet.isCompressable()) {
			for (World world : DimensionManager.getWorlds()) {
				for (Object playerObject : world.playerEntities) {
					EntityPlayer player = (EntityPlayer) playerObject;
					SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet, player);
				}
			}
		} else {
			MainProxy.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
			MainProxy.channels.get(Side.SERVER).writeOutbound(packet);
		}
	}

	public static EntityPlayer getFakePlayer(TileEntity tile) {
		return new FakePlayer(tile);
	}

	public static File getLPFolder() {
		return new File(DimensionManager.getCurrentSaveRootDirectory(), "LogisticsPipes");
	}

	public static void addTick() {
		MainProxy.globalTick++;
	}

	public static EntityItem dropItems(World worldObj, ItemStack stack, int xCoord, int yCoord, int zCoord) {
		EntityItem item = new EntityItem(worldObj, xCoord, yCoord, zCoord, stack);
		worldObj.spawnEntityInWorld(item);
		return item;
	}

	public static boolean checkPipesConnections(TileEntity from, TileEntity to) {
		return MainProxy.checkPipesConnections(from, to, OrientationsUtil.getOrientationOfTilewithTile(from, to));
	}

	public static boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way) {
		return MainProxy.checkPipesConnections(from, to, way, false);
	}

	public static boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way, boolean ignoreSystemDisconnection) {
		if (from == null || to == null) {
			return false;
		}
		IPipeInformationProvider fromInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(from);
		IPipeInformationProvider toInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(to);
		if (fromInfo == null && toInfo == null) {
			return false;
		}
		if (fromInfo != null) {
			if (!fromInfo.canConnect(to, way, ignoreSystemDisconnection)) {
				return false;
			}
		}
		if (toInfo != null) {
			if (!toInfo.canConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isPipeControllerEquipped(EntityPlayer entityplayer) {
		return entityplayer != null && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsPipeControllerItem;
	}
}
