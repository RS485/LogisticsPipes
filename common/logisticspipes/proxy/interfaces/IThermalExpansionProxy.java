package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import logisticspipes.recipes.CraftingParts;

public interface IThermalExpansionProxy {

	boolean isTE();

	CraftingParts getRecipeParts();

	boolean isToolHammer(Item stack);

	boolean canHammer(ItemStack stack, EntityPlayer entityplayer, BlockPos pos);

	void toolUsed(ItemStack stack, EntityPlayer entityplayer, BlockPos pos);
}
