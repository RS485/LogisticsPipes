package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.routing.LogisticsOrder;
import logisticspipes.utils.item.ItemIdentifierStack;

public interface IRequestWatcher {
	public void handleOrderList(ItemIdentifierStack stack, List<LogisticsOrder> orders);
	public void handleClientSideListInfo(int id, ItemIdentifierStack stack, List<LogisticsOrder> orders);
	public void handleClientSideRemove(int id);
}
