/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IProvide;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.request.IExtraPromise;
import logisticspipes.request.IPromise;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.item.ItemIdentifier;

public class LogisticsPromise implements IPromise {

	public ItemIdentifier item;
	public int numberOfItems;
	public IProvideItems sender;
	public ResourceType type;

	public LogisticsPromise(ItemIdentifier item, int numberOfItems, IProvideItems sender, ResourceType type) {
		this.item = item;
		this.numberOfItems = numberOfItems;
		this.sender = sender;
		this.type = type;
	}

	@Override
	public LogisticsPromise copy() {
		return new LogisticsPromise(item, numberOfItems, sender, type);
	}

	@Override
	public boolean matches(IResource requestType) {
		return requestType.matches(item, IResource.MatchSettings.NORMAL);
	}

	@Override
	public int getAmount() {
		return numberOfItems;
	}

	@Override
	public IExtraPromise split(int more) {
		numberOfItems -= more;
		return new LogisticsExtraPromise(getItemType(), more, sender, false);
	}

	@Override
	public IProvide getProvider() {
		return sender;
	}

	@Override
	public ItemIdentifier getItemType() {
		return item;
	}

	@Override
	public ResourceType getType() {
		return type;
	}

	@Override
	public IOrderInfoProvider fullFill(IResource requestType, IAdditionalTargetInformation info) {
		IRequestItems destination;
		if (requestType instanceof ItemResource) {
			destination = ((ItemResource) requestType).getTarget();
		} else if (requestType instanceof DictResource) {
			destination = ((DictResource) requestType).getTarget();
		} else {
			throw new UnsupportedOperationException();
		}
		return sender.fullFill(this, destination, info);
	}
}
