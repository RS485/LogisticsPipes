package logisticspipes.request;

import java.util.List;
import java.util.Map;

import logisticspipes.routing.LogisticsOrder;
import logisticspipes.utils.item.ItemIdentifier;

public interface RequestLog {
	public void handleMissingItems(Map<ItemIdentifier,Integer> items);
	public void handleSucessfullRequestOf(ItemIdentifier item, int count, List<LogisticsOrder> paticipating);
	public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items);
}
