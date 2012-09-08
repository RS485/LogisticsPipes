package logisticspipes.proxy;

import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;
import net.minecraft.src.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class MainProxy {
	
	@SidedProxy(clientSide="logisticspipes.proxy.ClientProxy", serverSide="logisticspipes.proxy.ServerProxy")
	public static IProxy proxy;

	public static boolean isClient(World world) {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}
	
	public static boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}
	
	public static boolean isServer(World world) {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}
	
	public static boolean isServer() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

	public static World getClientMainWorld() {
		return proxy.getWorld();
	}
	
	public static int getDimensionForWorld(World world) {
		if(world instanceof WorldServer) {
			return ((WorldServer)world).provider.worldType;
		}
		if(world instanceof WorldClient) {
			return ((WorldClient)world).provider.worldType;
		}
		return world.getWorldInfo().getDimension();
	}

	public static World getWorld(int _dimension) {
		return proxy.getWorld(_dimension);
	}
	
	public static void sendToPlayerList(Packet packet, List<EntityPlayer> players) {
		for(EntityPlayer player:players) {
			PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
		}
	}
}
