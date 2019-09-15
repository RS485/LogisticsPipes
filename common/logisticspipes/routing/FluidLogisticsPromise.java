/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.RequestProvider;
import logisticspipes.interfaces.routing.FluidRequestProvider;
import logisticspipes.request.ExtraPromise;
import logisticspipes.request.Promise;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.Resource;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;

public class FluidLogisticsPromise implements Promise {

	public FluidKey liquid;
	public int amount;
	public FluidRequestProvider sender;
	public ResourceType type;

	@Override
	public FluidLogisticsPromise copy() {
		FluidLogisticsPromise result = new FluidLogisticsPromise();
		result.liquid = liquid;
		result.amount = amount;
		result.sender = sender;
		result.type = type;
		return result;
	}

	@Override
	public boolean matches(Resource requestType) {
		if (requestType instanceof FluidResource) {
			FluidResource fluid = (FluidResource) requestType;
			return fluid.getFluid().equals(liquid);
		}
		return false;
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public ExtraPromise split(int more) {
		// TODO Add When Fluid crafing is supported
		throw new UnsupportedOperationException("Fluid Promises can't be split");
	}

	@Override
	public RequestProvider getProvider() {
		return sender;
	}

	@Override
	public ItemIdentifier getItemType() {
		return liquid.getItemIdentifier();
	}

	@Override
	public ResourceType getType() {
		return type;
	}

	@Override
	public IOrderInfoProvider fullFill(Resource requestType, IAdditionalTargetInformation info) {
		return sender.fulfill(this, ((FluidResource) requestType).getTarget(), type, info);
	}
}
