package logisticspipes.interfaces;

import logisticspipes.request.resources.IResource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;

public interface IRequestWatcher {

	public void handleOrderList(IResource stack, LinkedLogisticsOrderList orders);

	public void handleClientSideListInfo(int id, IResource stack, LinkedLogisticsOrderList orders);

	public void handleClientSideRemove(int id);
}
