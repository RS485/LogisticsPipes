package logisticspipes.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import buildcraft.core.DefaultProps;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketRenderFX;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.pipefxhandlers.providers.EntityBlueSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityGoldSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityGreenSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityOrangeSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityRedSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityVioletSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityWhiteSparkleFXProvider;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.ticks.PacketBufferHandlerThread;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.ServerListenThread;
import net.minecraft.src.ThreadMinecraftServer;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;
import net.minecraft.src.WorldServer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class MainProxy {
	
	@SidedProxy(clientSide="logisticspipes.proxy.side.ClientProxy", serverSide="logisticspipes.proxy.side.ServerProxy")
	public static IProxy proxy;
	
	private static HashMap<Thread, Side> threadSideMap = new HashMap<Thread, Side>();
	
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
        if ((thr instanceof ThreadMinecraftServer) || (thr instanceof ServerListenThread))
        {
            return Side.SERVER;
        }
        return Side.CLIENT;
    }
	
	public static boolean isClient(World world) {
		return isClient();
	}
	
	public static boolean isClient() {
		return getEffectiveSide() == Side.CLIENT;
	}
	
	public static boolean isServer(World world) {
		return isServer();
	}
	
	public static boolean isServer() {
		return getEffectiveSide() == Side.SERVER;
	}

	public static World getClientMainWorld() {
		return proxy.getWorld();
	}
	
	public static int getDimensionForWorld(World world) {
		if(world instanceof WorldServer) {
			return ((WorldServer)world).provider.dimensionId;
		}
		if(world instanceof WorldClient) {
			return ((WorldClient)world).provider.dimensionId;
		}
		return world.getWorldInfo().getDimension();
	}

	public static World getWorld(int _dimension) {
		return proxy.getWorld(_dimension);
	}
	
	public static void sendPacketToPlayer(Packet packet, Player player) {
		if(!isDirectSendPacket(packet)) {
			PacketBufferHandlerThread.addPacketToCompressor((Packet250CustomPayload) packet, player);
		} else {
			PacketDispatcher.sendPacketToPlayer(packet, player);
		}
	}
	
	public static void sendPacketToAllAround(double X, double Y, double Z, double range, int dimensionId, Packet packet) {
		if(!isDirectSendPacket(packet)) {
			new Exception("Packet size to big").printStackTrace();
		}
		PacketDispatcher.sendPacketToAllAround(X, Y, Z, range, dimensionId, packet);
	}
	
	public static void sendPacketToServer(Packet packet) {
		if(!isDirectSendPacket(packet)) {
			new Exception("Packet size to big").printStackTrace();
		}
		PacketDispatcher.sendPacketToServer(packet);
	}
	
	public static void sendToPlayerList(Packet packet, List<EntityPlayer> players) {
		for(EntityPlayer player:players) {
			if(!isDirectSendPacket(packet)) {
				PacketBufferHandlerThread.addPacketToCompressor((Packet250CustomPayload) packet, (Player) player);
			} else {
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

	public static void sendCompressedToAllPlayers(Packet250CustomPayload packet) {
		for(World world: DimensionManager.getWorlds()) {
			for(Object playerObject:world.playerEntities) {
				Player player = (Player) playerObject;
				PacketBufferHandlerThread.addPacketToCompressor(packet, player);
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

	public static void sendSpawnParticlePacket(int particle, int xCoord, int yCoord, int zCoord, World dimension, int amount) {
		if(MainProxy.isServer()) {
			MainProxy.sendPacketToAllAround(xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE, MainProxy.getDimensionForWorld(dimension), new PacketRenderFX(NetworkConstants.PARTICLE_FX_RENDER_DATA, xCoord, yCoord, zCoord, particle, amount).getPacket());
		} else {
			LogisticsPipes.log.severe("Server only method on Client (Particle Spawning)");
		}
	}
	
	public static void spawnParticle(int particle, int xCoord, int yCoord, int zCoord, int amount) {
		if(MainProxy.isClient()) {
			PipeFXRenderHandler.spawnGenericParticle(particle, xCoord, yCoord, zCoord, amount);
		} else {
			LogisticsPipes.log.severe("Client only method on Server (Particle Spawning)");
		}
	}
}
