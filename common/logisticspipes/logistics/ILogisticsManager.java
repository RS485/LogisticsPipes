/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logistics;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;

public interface ILogisticsManager {

	IRoutedItem assignDestinationFor(IRoutedItem item, int sourceRouterint, boolean excludeSource);

	LinkedList<ItemIdentifier> getCraftableItems(List<ExitRoute> list);

	Map<ItemIdentifier, Integer> getAvailableItems(List<ExitRoute> list);

	String getBetterRouterName(IRouter r);

	int getAmountFor(ItemIdentifier item, List<ExitRoute> validDestinations);
	//boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, boolean realrequest, boolean denyCrafterAdding);
	//boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors);
	//boolean request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors);
}
