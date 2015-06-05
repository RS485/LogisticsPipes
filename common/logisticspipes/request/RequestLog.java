package logisticspipes.request;

import java.util.List;

import logisticspipes.request.resources.IResource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;

public interface RequestLog {

	public void handleMissingItems(List<IResource> resources);

	public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList paticipating);

	public void handleSucessfullRequestOfList(List<IResource> resources, LinkedLogisticsOrderList paticipating);
}
