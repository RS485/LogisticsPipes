package logisticspipes.proxy.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;

public interface IToolWrenchProxy {

	public boolean isWrenchEquipped(EntityPlayer entityplayer);

	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z);

	public void wrenchUsed(EntityPlayer entityplayer, BlockPos pos);

	public boolean isWrench(Item item);
}
