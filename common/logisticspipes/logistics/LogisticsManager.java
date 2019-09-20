/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logistics;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.Router;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.tuples.Tuple3;
import network.rs485.logisticspipes.util.ItemVariant;

public interface LogisticsManager {

	static LogisticsManager getInstance() {
		return LogisticsManagerImpl.INSTANCE;
	}

	IRoutedItem assignDestinationFor(IRoutedItem item, UUID sourceRouterId, boolean excludeSource);

	Tuple3<Integer, SinkReply, List<IFilter>> hasDestination(ItemStack stack, boolean allowDefault, UUID sourceRouterId, List<Integer> routerIDsToExclude);

	Tuple3<Integer, SinkReply, List<IFilter>> hasDestinationWithMinPriority(ItemStack stack, UUID sourceRouterId, boolean excludeSource, FixedPriority priority);

	LinkedList<ItemVariant> getCraftableItems(List<ExitRoute> list);

	Set<ItemStack> getAvailableItems(List<ExitRoute> list);

	String getBetterRouterName(Router r);

	int getAmountFor(ItemVariant item, List<ExitRoute> validDestinations);

	// boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, boolean realrequest, boolean denyCrafterAdding);

	// boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors);

	// boolean request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors);

}
