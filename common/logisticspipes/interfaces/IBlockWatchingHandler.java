package logisticspipes.interfaces;

import net.minecraft.src.EntityPlayer;

public interface IBlockWatchingHandler {
	public void playerStartWatching(EntityPlayer player);
	public void playerStopWatching(EntityPlayer player);
}
