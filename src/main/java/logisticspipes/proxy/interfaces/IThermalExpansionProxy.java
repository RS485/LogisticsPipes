package logisticspipes.proxy.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import logisticspipes.recipes.CraftingParts;

public interface IThermalExpansionProxy {

	boolean isTE();

	CraftingParts getRecipeParts();

	boolean isToolHammer(Item stack);

	boolean canHammer(@Nonnull ItemStack stack, EntityPlayer entityplayer, BlockPos pos);

	void toolUsed(@Nonnull ItemStack stack, EntityPlayer entityplayer, BlockPos pos);
}
