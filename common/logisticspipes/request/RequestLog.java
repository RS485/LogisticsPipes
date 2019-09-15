package logisticspipes.request;

import java.util.List;

import logisticspipes.request.resources.Resource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;

public interface RequestLog {

	void handleMissingItems(List<Resource> resources);

	void handleSucessfullRequestOf(Resource item, LinkedLogisticsOrderList paticipating);

	void handleSucessfullRequestOfList(List<Resource> resources, LinkedLogisticsOrderList paticipating);
}
