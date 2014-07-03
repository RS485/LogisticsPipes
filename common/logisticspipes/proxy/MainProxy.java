package logisticspipes.proxy;

import java.io.File;
import java.util.WeakHashMap;

import logisticspipes.LogisticsEventListener;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.FakePlayer;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.routing.debug.RoutingTableDebugUpdateThread;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.PlayerCollectionList;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.ServerListenThread;
import net.minecraft.server.ThreadMinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class MainProxy {
	
	@SidedProxy(clientSide="logisticspipes.proxy.side.ClientProxy", serverSide="logisticspipes.proxy.side.ServerProxy")
	public static IProxy proxy;
	@Getter
	private static int	globalTick;
	
	private static WeakHashMap<Thread, Side> threadSideMap = new WeakHashMap<Thread, Side>();
	
	private static Side getEffectiveSide() {
		Thread thr = Thread.currentThread();
		if(threadSideMap.containsKey(thr)) {
			return threadSideMap.get(thr);
		}
		Side side = getEffectiveSide(thr);
		if(threadSideMap.size() > 50) {
			threadSideMap.clear();
		}
		threadSideMap.put(thr, side);
		return side;
	}
	
	private static Side getEffectiveSide(Thread thr) {
        if(SimpleServiceLocator.ccProxy != null && SimpleServiceLocator.ccProxy.isLuaThread(thr)) {
        	return Side.SERVER;
        }
        if ((thr instanceof ThreadMinecraftServer) || (thr instanceof ServerListenThread) || (thr instanceof RoutingTableUpdateThread) || (thr instanceof RoutingTableDebugUpdateThread))
        {
            return Side.SERVER;
        }
        return Side.CLIENT;
    }
	
	public static boolean isClient(World world) {
		try{
			return world.isRemote;
		} catch(NullPointerException n) {
			LogisticsPipes.log.severe("isClient called with a null world - using slow thread based fallback");
			n.printStackTrace();
		}
		return isClient();
	}
	
	@Deprecated 
	/**
	 * isClient is slow, find a world and check isServer(world)
	 * @return
	 */
	public static boolean isClient() {
		return getEffectiveSide() == Side.CLIENT;
	}
	
	public static boolean isServer(World world) {
		try{
			return !world.isRemote;
		} catch(NullPointerException n) {
			LogisticsPipes.log.severe("isServer called with a null world - using slow thread based fallback");
			n.printStackTrace();
		}
		return isServer();
	}

	
	@Deprecated 
	/**
	 * isServer is slow, find a world and check isServer(world)
	 * @return
	 */
	public static boolean isServer() {
		return getEffectiveSide() == Side.SERVER;
	}

	public static World getClientMainWorld() {
		return proxy.getWorld();
	}
	
	public static int getDimensionForWorld(World world) {
		return proxy.getDimensionForWorld(world);
	}

	public static void sendPacketToServer(ModernPacket packet) {
		packet.create();
		if(packet.isCompressable() || needsToBeCompressed(packet)) {
			SimpleServiceLocator.clientBufferHandler.addPacketToCompressor(packet.getPacket());
		} else {
			PacketDispatcher.sendPacketToServer(packet.getPacket());
		}
	}

	public static void sendPacketToPlayer(ModernPacket packet, EntityPlayer player) {
		packet.create();
		if(packet.isCompressable() || needsToBeCompressed(packet)) {
			SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet.getPacket(), player);
		} else {
			PacketDispatcher.sendPacketToPlayer(packet.getPacket(), (Player) player);
		}
	}

	public static void sendPacketToAllWatchingChunk(int X, int Z, int dimensionId, ModernPacket packet) {
		packet.create();
		ChunkCoordIntPair chunk = new ChunkCoordIntPair(X >> 4, Z >> 4);
		PlayerCollectionList players = LogisticsEventListener.watcherList.get(chunk);
		if(players != null) {
			for(EntityPlayer player:players.players()) {
				if(MainProxy.getDimensionForWorld(player.worldObj) == dimensionId) {
					sendPacketToPlayer(packet, player);
				}
			}
			return;
		}
	}
	
	public static void sendToPlayerList(ModernPacket packet, PlayerCollectionList players) {
		if(players.isEmpty()) return;
		packet.create();
		if(packet.isCompressable() || needsToBeCompressed(packet)) {
			for(EntityPlayer player:players.players()) {
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet.getPacket(), player);
			}
		} else {
			for(EntityPlayer player:players.players()) {
				PacketDispatcher.sendPacketToPlayer(packet.getPacket(), (Player) player);
			}
		}
	}

	public static void sendToAllPlayers(ModernPacket packet) {
		packet.create();
		if(packet.isCompressable() || needsToBeCompressed(packet)) {
			for(World world: DimensionManager.getWorlds()) {
				for(Object playerObject:world.playerEntities) {
					EntityPlayer player = (EntityPlayer) playerObject;
					SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet.getPacket(), player);
				}
			}
		} else {
			PacketDispatcher.sendPacketToAllPlayers(packet.getPacket());
		}
	}

	private static boolean needsToBeCompressed(ModernPacket packet) {
		if(packet.getData() != null) {
			if(packet.getData().length > 32767) {
				return true; // Packet is to big
			}
		}
		return false;
	}

	public static EntityPlayer getFakePlayer(TileEntity tile) {
		return new FakePlayer(tile);
	}

	public static File getLPFolder() {
		return new File(DimensionManager.getCurrentSaveRootDirectory(), "LogisticsPipes");
	}

	public static void addTick() {
		globalTick++;
	}
}

