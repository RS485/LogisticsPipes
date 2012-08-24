package logisticspipes.buildcraft.krapht.proxy;

import cpw.mods.fml.common.SidedProxy;

public class MainProxy {
	
	@SidedProxy(clientSide="logisticspipes.buildcraft.krapht.proxy.ClientProxy", serverSide="logisticspipes.buildcraft.krapht.proxy.ServerProxy")
	public static IProxy proxy;
	
	public static boolean isClient() {
		return proxy.getSide().equals("Client");
	}
	
	public static boolean isServer() {
		return proxy.getSide().equals("Server");
	}
}
