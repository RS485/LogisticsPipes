/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.request.CraftingTemplate;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifier;

public interface ICraftItems extends IProvideItems, IRequestItems{
	void registerExtras(LogisticsPromise promise);
	CraftingTemplate addCrafting(ItemIdentifier toCraft);
	//void canCraft(LogisticsTransaction transaction);
	List<ItemIdentifier> getCraftedItems();
	int getTodo();
}
