package appeng.api.me.tiles;

import net.minecraft.item.ItemStack;

/**
 * An IPushable should return what dosn't fit, so that the crafting request cannot complete,
 *  stalling the action and saving your resources.
 */
public interface IPushable
{
	ItemStack pushItem( ItemStack out );
	boolean canPushItem(ItemStack out);
}
