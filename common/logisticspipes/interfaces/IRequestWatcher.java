package logisticspipes.interfaces;

import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifierStack;

public interface IRequestWatcher {
	public void handleOrderList(ItemIdentifierStack stack, LinkedLogisticsOrderList orders);
	public void handleClientSideListInfo(int id, ItemIdentifierStack stack, LinkedLogisticsOrderList orders);
	public void handleClientSideRemove(int id);
}
