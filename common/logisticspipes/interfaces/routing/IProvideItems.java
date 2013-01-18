/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifier;

public interface IProvideItems {
	public void canProvide(RequestTreeNode tree, Map<ItemIdentifier, Integer> donePromisses);
	public void fullFill(LogisticsPromise promise, IRequestItems destination);
	public int getAvailableItemCount(ItemIdentifier item);
	//public HashMap<ItemIdentifier, Integer> getAllItems();
	public void getAllItems(ArrayList<Map<ItemIdentifier, Integer>> items);
}
