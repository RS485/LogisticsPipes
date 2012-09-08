package logisticspipes.interfaces;

import net.minecraft.src.EntityPlayer;

public interface IWatchingHandler {
	public void playerStartWatching(EntityPlayer player, int mode);
	public void playerStopWatching(EntityPlayer player, int mode);
}
