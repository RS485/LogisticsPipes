/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface ItemCrafter extends Crafter, ItemRequestProvider, ItemSpaceControl, IRequireReliableTransport {

	/**
	 * some items do not have a specific list of things they can craft (ie, AE
	 * crafting system, fuzzy crafting) in this case enumerating them all will
	 * be slow/impossible.
	 *
	 * @deprecated getCraftedItems can be slow, instead ask if the item you are
	 *             after can be crafted
	 */
	@Deprecated
	List<ItemStack> getCraftedItems(); // list of all items that can be crafted.

}
