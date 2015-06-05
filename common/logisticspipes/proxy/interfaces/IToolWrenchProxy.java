package logisticspipes.proxy.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public interface IToolWrenchProxy {

	public boolean isWrenchEquipped(EntityPlayer entityplayer);

	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z);

	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z);

	public boolean isWrench(Item item);
}
