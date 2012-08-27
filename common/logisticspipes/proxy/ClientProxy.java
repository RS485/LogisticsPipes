package logisticspipes.proxy;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.src.World;


public class ClientProxy implements IProxy {
	@Override
	public String getSide() {
		return "Client";
	}

	@Override
	public World getWorld() {
		return FMLClientHandler.instance().getClient().theWorld;
	}
}
