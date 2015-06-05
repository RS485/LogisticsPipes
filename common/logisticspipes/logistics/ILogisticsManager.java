/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logistics;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Triplet;

public interface ILogisticsManager {

	IRoutedItem assignDestinationFor(IRoutedItem item, int sourceRouterint, boolean excludeSource);

	Triplet<Integer, SinkReply, List<IFilter>> hasDestination(ItemIdentifier stack, boolean allowDefault, int sourceID, List<Integer> routerIDsToExclude);

	Triplet<Integer, SinkReply, List<IFilter>> hasDestinationWithMinPriority(ItemIdentifier stack, int sourceRouter, boolean excludeSource, FixedPriority priority);

	LinkedList<ItemIdentifier> getCraftableItems(List<ExitRoute> list);

	Map<ItemIdentifier, Integer> getAvailableItems(List<ExitRoute> list);

	String getBetterRouterName(IRouter r);

	int getAmountFor(ItemIdentifier item, List<ExitRoute> validDestinations);
	//boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, boolean realrequest, boolean denyCrafterAdding);
	//boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors);
	//boolean request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors);
}
