package logisticspipes.interfaces;

import logisticspipes.request.resources.IResource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;

public interface IRequestWatcher {

	void handleOrderList(IResource stack, LinkedLogisticsOrderList orders);

	void handleClientSideListInfo(int id, IResource stack, LinkedLogisticsOrderList orders);

	void handleClientSideRemove(int id);
}
