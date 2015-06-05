package logisticspipes.interfaces;

import net.minecraft.entity.player.EntityPlayer;

public interface IWatchingHandler {

	public void playerStartWatching(EntityPlayer player, int mode);

	public void playerStopWatching(EntityPlayer player, int mode);
}
