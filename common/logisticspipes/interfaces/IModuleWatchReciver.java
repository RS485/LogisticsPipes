package logisticspipes.interfaces;

import net.minecraft.src.EntityPlayer;

public interface IModuleWatchReciver {
	public void startWatching(EntityPlayer player) ;
	public void stopWatching(EntityPlayer player);
}
