package logisticspipes.proxy.interfaces;

import net.minecraft.entity.player.EntityPlayer;

public interface IToolWrenchProxy {
	public boolean isWrenchEquipped(EntityPlayer entityplayer);
	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z);
	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z);
}
