package logisticspipes.proxy;

import java.util.WeakHashMap;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.FakePlayer;
import logisticspipes.config.Configs;
import logisticspipes.main.LogisticsEventListener;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.pipe.ParticleFX;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.PlayerCollectionList;
import net.minecraft.client.Minecraft;
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
        if ((thr instanceof ThreadMinecraftServer) || (thr instanceof ServerListenThread) || (thr instanceof RoutingTableUpdateThread))
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

	public static void sendPacketToPlayer(ModernPacket packet, Player player) {
		packet.create();
		if(packet.isCompressable() || needsToBeCompressed(packet)) {
			SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet.getPacket(), player);
		} else {
			PacketDispatcher.sendPacketToPlayer(packet.getPacket(), player);
		}
	}

	public static void sendPacketToAllWatchingChunk(int X, int Z, int dimensionId, ModernPacket packet) {
		packet.create();
		ChunkCoordIntPair chunk = new ChunkCoordIntPair(X >> 4, Z >> 4);
		PlayerCollectionList players = LogisticsEventListener.watcherList.get(chunk);
		if(players != null) {
			for(EntityPlayer player:players.players()) {
				if(MainProxy.getDimensionForWorld(player.worldObj) == dimensionId) {
					sendPacketToPlayer(packet, (Player)player);
				}
			}
			return;
		}
	}
	
	public static void sendToPlayerList(ModernPacket packet, PlayerCollectionList players) {
		packet.create();
		if(packet.isCompressable() || needsToBeCompressed(packet)) {
			for(EntityPlayer player:players.players()) {
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet.getPacket(), (Player) player);
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
					Player player = (Player) playerObject;
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

	public static void sendSpawnParticlePacket(int particle, int xCoord, int yCoord, int zCoord, World dimension, int amount) {
		if(!Configs.ENABLE_PARTICLE_FX) return;
		if(MainProxy.isServer(dimension)) {
			MainProxy.sendPacketToAllWatchingChunk(xCoord, zCoord, MainProxy.getDimensionForWorld(dimension), PacketHandler.getPacket(ParticleFX.class).setInteger2(amount).setInteger(particle).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
		} else {
			LogisticsPipes.log.severe("Server only method on Client (Particle Spawning)");
		}
	}
	
	public static void spawnParticle(int particle, int xCoord, int yCoord, int zCoord, int amount) {
		if(!Configs.ENABLE_PARTICLE_FX || !Minecraft.isFancyGraphicsEnabled()) return;
		PipeFXRenderHandler.spawnGenericParticle(particle, xCoord, yCoord, zCoord, amount);
	}
	
	public static EntityPlayer getFakePlayer(TileEntity tile) {
		return new FakePlayer(tile);
	}
}

