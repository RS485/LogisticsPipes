/**
 * Copyright (c) Krapht, 2011
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.items;

import java.util.List;

import logisticspipes.utils.string.StringUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;

/**
 * A logistics pipe Item
 */
public class ItemLogisticsPipe extends ItemPipe {
	public ItemLogisticsPipe(int key, Class<? extends Pipe<?>> clas) {
		super(key);
		//setCreativeTab(LogisticsPipes.LPCreativeTab);
		//setUnlocalizedName(clas.getSimpleName());
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.translate(getUnlocalizedName(itemstack));
	}

	/**
	 * Adds all keys from the translation file in the format:
	 *  item.className.tip([0-9]*)
	 *
	 * Tips start from 1 and increment. Sparse rows should be left empty (ie empty line must still have a key present)
	 *
	 * Shift shows full tooltip, without it you just get the first line.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flags) {
		StringUtil.addShiftAddition(stack, list);
	}
}
