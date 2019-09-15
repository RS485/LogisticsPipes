package logisticspipes.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * public interface to be implemented by an item which can open the config GUI for a logistics pipe.
 * Some mod compatibility is already implemented inside LP.
 */
public interface ILPPipeConfigTool {

	default boolean canWrench(PlayerEntity player, ItemStack wrench, ILPPipeTile pipe) {
		return true;
	}

	default void wrenchUsed(PlayerEntity player, ItemStack wrench, ILPPipeTile pipe) {}

}
