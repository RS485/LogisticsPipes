package logisticspipes.routing.order;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.ILPPositionProvider;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class LogisticsItemOrderManager extends LogisticsOrderManager<LogisticsItemOrder> {

	public LogisticsItemOrderManager(ILPPositionProvider pos) {
		super(pos);
	}

	public LogisticsItemOrderManager(IChangeListener listener, ILPPositionProvider pos) {
		super(listener, pos);
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

	@Override
	public LogisticsItemOrder peekAtTopRequest(ResourceType type) {
		return super.peekAtTopRequest(type);
	}

	public int totalItemsCountInOrders(ItemIdentifier item) {
		int itemCount = 0;
		for (LogisticsItemOrder request : _orders) {
			if (!request.getItemStack().getItem().equals(item)) {
				continue;
			}
			itemCount += request.getItemStack().getStackSize();
		}
		return itemCount;
	}
}
