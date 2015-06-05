package logisticspipes.interfaces;

import net.minecraft.entity.player.EntityPlayer;

public interface IModuleWatchReciver {

	public void startWatching(EntityPlayer player);

	public void stopWatching(EntityPlayer player);
}
