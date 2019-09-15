/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ItemRequestProvider;
import logisticspipes.interfaces.routing.ItemRequester;
import logisticspipes.interfaces.routing.RequestProvider;
import logisticspipes.request.ExtraPromise;
import logisticspipes.request.Promise;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import network.rs485.logisticspipes.routing.request.Resource;
import network.rs485.logisticspipes.util.ItemVariant;

public class LogisticsPromise implements Promise {

	public ItemStack stack;
	public ItemRequestProvider sender;
	public ResourceType type;

	public LogisticsPromise(ItemStack stack, ItemRequestProvider sender, ResourceType type) {
		this.stack = stack;
		this.sender = sender;
		this.type = type;
	}

	@Override
	public LogisticsPromise copy() {
		return new LogisticsPromise(stack, sender, type);
	}

	@Override
	public boolean matches(Resource requestType) {
		return requestType.matchesAny(stack, false);
	}

	@Override
	public int getAmount() {
		return stack.getCount();
	}

	@Override
	public ExtraPromise split(int more) {
		return new LogisticsExtraPromise(stack.split(more), sender, false);
	}

	@Override
	public RequestProvider getProvider() {
		return sender;
	}

	@Override
	public ItemVariant getItemType() {
		return ItemVariant.fromStack(stack);
	}

	@Override
	public ResourceType getType() {
		return type;
	}

	@Override
	public IOrderInfoProvider fullFill(Resource requestType, IAdditionalTargetInformation info) {
		ItemRequester destination;
		if (requestType instanceof Resource.Item) {
			destination = ((Resource.Item) requestType).getRequester();
		} else if (requestType instanceof Resource.Dict) {
			destination = ((Resource.Dict) requestType).getRequester();
		} else {
			throw new IllegalArgumentException(String.format("Can't handle %s", requestType));
		}
		return sender.fulfill(this, destination, info);
	}
}
