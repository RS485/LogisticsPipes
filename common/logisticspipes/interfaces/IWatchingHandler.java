package logisticspipes.interfaces;

import net.minecraft.entity.player.EntityPlayer;

public interface IWatchingHandler {

	void playerStartWatching(EntityPlayer player, int mode);

	void playerStopWatching(EntityPlayer player, int mode);
}
