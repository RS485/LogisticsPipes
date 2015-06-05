package logisticspipes.interfaces;

import net.minecraft.entity.player.EntityPlayer;

public interface IBlockWatchingHandler {

	public void playerStartWatching(EntityPlayer player);

	public void playerStopWatching(EntityPlayer player);
}
