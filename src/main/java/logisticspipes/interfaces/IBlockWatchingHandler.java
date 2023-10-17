package logisticspipes.interfaces;

import net.minecraft.entity.player.EntityPlayer;

public interface IBlockWatchingHandler {

	void playerStartWatching(EntityPlayer player);

	void playerStopWatching(EntityPlayer player);
}
