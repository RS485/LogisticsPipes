package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public interface INEIProxy {
	public ItemStack getItemForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver);
	public List<String> getInfoForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver);
}
