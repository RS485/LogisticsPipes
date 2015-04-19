package logisticspipes.routing.order;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;

public class LogisticsItemOrder extends LogisticsOrder {

	public LogisticsItemOrder(ItemIdentifierStack item, IRequestItems destination, ResourceType type, IAdditionalTargetInformation info) {
		super(type, info);
		if(item == null) {
			throw new NullPointerException();
		}
		this.itemStack = item;
		this.destination = destination;
	}
	
	@Getter
	private final ItemIdentifierStack itemStack;
	@Getter
	private final IRequestItems destination;
	
	@Override
	public IRouter getRouter() {
		return destination.getRouter();
	}

	@Override
	public void sendFailed() {
		destination.itemCouldNotBeSend(getItemStack(), getInformation());
	}

	@Override
	public ItemIdentifierStack getAsDisplayItem() {
		return itemStack;
	}

	@Override
	public int getAmount() {
		return itemStack.getStackSize();
	}

	@Override
	public void reduceAmountBy(int amount) {
		itemStack.setStackSize(itemStack.getStackSize() - amount);
	}
}
