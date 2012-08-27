package logisticspipes.proxy;

import net.minecraft.src.World;

public class ServerProxy implements IProxy {
	@Override
	public String getSide() {
		return "Server";
	}

	@Override
	public World getWorld() {
		return null;
	}
}
