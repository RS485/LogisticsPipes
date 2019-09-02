package logisticspipes.request;

import java.util.List;

import logisticspipes.request.resources.IResource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;

public interface RequestLog {

	void handleMissingItems(List<IResource> resources);

	void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList paticipating);

	void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList paticipating);
}
