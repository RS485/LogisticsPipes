package logisticspipes.routing.order;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class LogisticsItemOrderManager extends LogisticsOrderManager<LogisticsItemOrder> {

	public LogisticsItemOrderManager() {
		super();
	}
	
	public LogisticsItemOrderManager(IChangeListener listener) {
		super(listener);
	}
	
	@Override
	public void sendFailed() {
		_orders.getFirst().sendFailed();
		super.sendFailed();
	}

	public LogisticsItemOrder addOrder(ItemIdentifierStack stack, IRequestItems requester, ResourceType type, IAdditionalTargetInformation info) {
		LogisticsItemOrder order = new LogisticsItemOrder(stack, requester, type, info);
		_orders.addLast(order);
		listen();
		return order;
	}
	
	public LogisticsItemOrder peekAtTopRequest(ResourceType type) {
		return (LogisticsItemOrder) super.peekAtTopRequest(type);
	}
	
	public int totalItemsCountInOrders(ItemIdentifier item) {
		int itemCount = 0;
		for(LogisticsItemOrder request: _orders) {
			if(!request.getItemStack().getItem().equals(item)) continue;
			itemCount += request.getItemStack().getStackSize();
		}
		return itemCount;
	}
}
