package logisticspipes.api;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * public interface to be implemented by an item which can open the config GUI for a logistics pipe.
 * Some mod compatibility is already implemented inside LP.
 */
public interface ILPPipeConfigTool {

	boolean canWrench(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe);

	void wrenchUsed(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe);
}
