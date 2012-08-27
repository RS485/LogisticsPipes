/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import java.util.HashMap;

import logisticspipes.main.LogisticsPromise;
import logisticspipes.main.LogisticsTransaction;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifier;



public interface IProvideItems {
	public void canProvide(LogisticsTransaction transaction);
	public void fullFill(LogisticsPromise promise, IRequestItems destination);
	public int getAvailableItemCount(ItemIdentifier item);
	public HashMap<ItemIdentifier, Integer> getAllItems();
	public IRouter getRouter();
}
