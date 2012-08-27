package logisticspipes.proxy;

import net.minecraft.src.World;
import net.minecraft.src.WorldClient;
import net.minecraft.src.WorldServer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;

public class MainProxy {
	
	//@SidedProxy(clientSide="logisticspipes.buildcraft.krapht.proxy.ClientProxy", serverSide="logisticspipes.buildcraft.krapht.proxy.ServerProxy")
	public static IProxy proxy;
	
	public static boolean isClient(World world) {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}
	
	public static boolean isServer(World world) {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}
}
