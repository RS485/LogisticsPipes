package logisticspipes.proxy;

import net.minecraft.src.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;

public class MainProxy {
	
	@SidedProxy(clientSide="logisticspipes.proxy.ClientProxy", serverSide="logisticspipes.proxy.ServerProxy")
	public static IProxy proxy;
	
	public static boolean isClient(World world) {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}
	
	public static boolean isServer(World world) {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

	public static World getClientMainWorld() {
		return proxy.getWorld();
	}
}
