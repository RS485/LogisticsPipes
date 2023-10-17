package logisticspipes.interfaces;

import net.minecraft.entity.player.EntityPlayer;

public interface IModuleWatchReciver {

	void startWatching(EntityPlayer player);

	void stopWatching(EntityPlayer player);
}
