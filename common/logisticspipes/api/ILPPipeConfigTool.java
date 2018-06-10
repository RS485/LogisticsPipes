package logisticspipes.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * public interface to be implemented by an item which can open the config GUI for a logistics pipe.
 * Some mod compatibility is already implemented inside LP.
 */
public interface ILPPipeConfigTool {
	boolean canWrench(EntityPlayer player, ItemStack wrench, ILPPipeTile pipe);
	void wrenchUsed(EntityPlayer player, ItemStack wrench, ILPPipeTile pipe);
}
