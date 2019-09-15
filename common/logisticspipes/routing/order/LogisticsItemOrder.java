package logisticspipes.routing.order;

import lombok.Getter;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ItemRequester;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.routing.Router;
import logisticspipes.utils.item.ItemStack;

public class LogisticsItemOrder extends LogisticsOrder {

	public LogisticsItemOrder(Resource.Dict item, ItemRequester destination, ResourceType type, IAdditionalTargetInformation info) {
		super(type, info);
		if (item == null) {
			throw new NullPointerException();
		}
		resource = item;
		this.destination = destination;
	}

	@Getter
	private final Resource.Dict resource;
	@Getter
	private final ItemRequester destination;

	@Override
	public Router getRouter() {
		if (destination == null) {
			return null;
		}
		return destination.getRouter();
	}

	@Override
	public void sendFailed() {
		if (destination == null) {
			return;
		}
		destination.itemCouldNotBeSend(getResource().stack, getInformation());
	}

	@Override
	public ItemStack getAsDisplayItem() {
		return resource.stack;
	}

	@Override
	public int getAmount() {
		return resource.stack.getStackSize();
	}

	@Override
	public void reduceAmountBy(int amount) {
		resource.stack.setStackSize(resource.stack.getStackSize() - amount);
	}
}
