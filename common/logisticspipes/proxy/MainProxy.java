package logisticspipes.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.main.LogisticsEventListener;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketRenderFX;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.ticks.RoutingTableUpdateThread;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.ServerListenThread;
import net.minecraft.server.ThreadMinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class MainProxy {
	
	@SidedProxy(clientSide="logisticspipes.proxy.side.ClientProxy", serverSide="logisticspipes.proxy.side.ServerProxy", bukkitSide="logisticspipes.proxy.side.BukkitProxy")
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
		/*
		if(world instanceof WorldServer) {
			return ((WorldServer)world).provider.dimensionId;
		}
		if(world instanceof WorldClient) {
			return ((WorldClient)world).provider.dimensionId;
		}
		return world.getWorldInfo().getDimension();
		*/
		return proxy.getDimensionForWorld(world);
	}

	public static void sendPacketToServer(Packet packet) {
		if(!isDirectSendPacket(packet)) {
			new Exception("Packet size too big").printStackTrace();
		}
		PacketDispatcher.sendPacketToServer(packet);
	}

	public static void sendCompressedPacketToServer(Packet250CustomPayload packet) {
		SimpleServiceLocator.clientBufferHandler.addPacketToCompressor(packet);
	}

	public static void sendPacketToPlayer(Packet packet, Player player) {
		if(!isDirectSendPacket(packet)) {
			SimpleServiceLocator.serverBufferHandler.addPacketToCompressor((Packet250CustomPayload) packet, player);
		} else {
			PacketDispatcher.sendPacketToPlayer(packet, player);
		}
	}

	public static void sendCompressedPacketToPlayer(Packet packet, Player player) {
		if(packet instanceof Packet250CustomPayload) {
			SimpleServiceLocator.serverBufferHandler.addPacketToCompressor((Packet250CustomPayload) packet, player);
		} else {
			PacketDispatcher.sendPacketToPlayer(packet, player);
		}
	}

	public static void sendPacketToAllWatchingChunk(int X, int Z, int dimensionId, Packet packet) {
		ChunkCoordIntPair chunk = new ChunkCoordIntPair(X >> 4, Z >> 4);
		List<EntityPlayer> players = LogisticsEventListener.watcherList.get(chunk);
		if(players != null) {
			for(EntityPlayer player:players) {
				if(MainProxy.getDimensionForWorld(player.worldObj) == dimensionId) {
					sendPacketToPlayer(packet, (Player)player);
				}
			}
			return;
		}
		if(!isDirectSendPacket(packet)) {
			new Exception("Packet size too big").printStackTrace();
		}
		PacketDispatcher.sendPacketToAllAround(X, 64, Z, 128, dimensionId, packet);
	}
	
	public static void sendToPlayerList(Packet packet, List<EntityPlayer> players) {
		if(!isDirectSendPacket(packet)) {
			for(EntityPlayer player:players) {
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor((Packet250CustomPayload) packet, (Player) player);
			}
		} else {
			for(EntityPlayer player:players) {
				PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
			}
		}
	}

	public static void sendCompressedToPlayerList(Packet packet, List<EntityPlayer> players) {
		if(packet instanceof Packet250CustomPayload) {
			for(EntityPlayer player:players) {
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor((Packet250CustomPayload) packet, (Player) player);
			}
		} else {
			for(EntityPlayer player:players) {
				PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
			}
		}
	}

	public static void sendToAllPlayers(Packet packet) {
		if(!isDirectSendPacket(packet)) {
			new Exception("Packet size to big").printStackTrace();
		}
		PacketDispatcher.sendPacketToAllPlayers(packet);
	}

	public static void sendCompressedToAllPlayers(Packet250CustomPayload packet) {
		for(World world: DimensionManager.getWorlds()) {
			for(Object playerObject:world.playerEntities) {
				Player player = (Player) playerObject;
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(packet, player);
			}
		}
	}

	private static boolean isDirectSendPacket(Packet packet) {
		if(packet instanceof Packet250CustomPayload) {
			Packet250CustomPayload packet250 = (Packet250CustomPayload) packet;
			if(packet250.data != null) {
				if(packet250.data.length > 32767 && packet250.channel.equals("BCLP")) {
					return false;
				}
			}
		}
		return true;
	}

	public static List<EntityPlayer> getPlayerArround(World worldObj, int xCoord, int yCoord, int zCoord, int distance) {
		List<EntityPlayer> list = new ArrayList<EntityPlayer>();
		if(worldObj != null) {
			for(Object playerObject:worldObj.playerEntities) {
				EntityPlayer player = (EntityPlayer) playerObject;
				if(Math.hypot(player.posX - xCoord, Math.hypot(player.posY - yCoord, player.posZ - zCoord)) < distance) {
					list.add(player);
				}
			}
		}
		return list;
	}

	public static void sendSpawnParticlePacket(int particle, int xCoord, int yCoord, int zCoord, World dimension, int amount) {
		if(!Configs.ENABLE_PARTICLE_FX) return;
		if(MainProxy.isServer(dimension)) {
			MainProxy.sendPacketToAllWatchingChunk(xCoord, zCoord, MainProxy.getDimensionForWorld(dimension), new PacketRenderFX(NetworkConstants.PARTICLE_FX_RENDER_DATA, xCoord, yCoord, zCoord, particle, amount).getPacket());
		} else {
			LogisticsPipes.log.severe("Server only method on Client (Particle Spawning)");
		}
	}
	
	public static void spawnParticle(int particle, int xCoord, int yCoord, int zCoord, int amount) {
		if(!Configs.ENABLE_PARTICLE_FX || !Minecraft.isFancyGraphicsEnabled()) return;
		PipeFXRenderHandler.spawnGenericParticle(particle, xCoord, yCoord, zCoord, amount);
	}
}
