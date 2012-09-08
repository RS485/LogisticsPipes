package logisticspipes.proxy;

import net.minecraft.src.World;

public interface IProxy {
	public String getSide();
	public World getWorld();
	public void registerTileEntitis();
	public World getWorld(int _dimension);
}