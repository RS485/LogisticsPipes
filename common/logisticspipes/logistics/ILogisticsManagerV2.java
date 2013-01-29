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
import logisticspipes.routing.IRouter;
import logisticspipes.routing.SearchNode;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.item.ItemStack;

public interface ILogisticsManagerV2 {

	IRoutedItem assignDestinationFor(IRoutedItem item, int sourceRouterint, boolean excludeSource);
	IRoutedItem destinationUnreachable(IRoutedItem item, int currentRouter);
	Pair3<Integer, SinkReply, List<IFilter>> hasDestination(ItemStack stack, boolean allowDefault, int uuid, boolean excludeSource);
	Pair3<Integer, SinkReply, List<IFilter>> hasDestinationWithMinPriority(ItemStack stack, int sourceRouter, boolean excludeSource, FixedPriority priority);
	LinkedList<ItemIdentifier> getCraftableItems(List<SearchNode> list);
	Map<ItemIdentifier, Integer> getAvailableItems(List<SearchNode> list);
	String getBetterRouterName(IRouter r);
	//boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, boolean realrequest, boolean denyCrafterAdding);
	//boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors);
	//boolean request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors);
}
