package logisticspipes.interfaces;

import logisticspipes.request.resources.Resource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;

public interface IRequestWatcher {

	void handleOrderList(Resource stack, LinkedLogisticsOrderList orders);

	void handleClientSideListInfo(int id, Resource stack, LinkedLogisticsOrderList orders);

	void handleClientSideRemove(int id);
}
