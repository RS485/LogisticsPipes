package logisticspipes.request;

import java.util.Map;

import logisticspipes.utils.item.ItemIdentifier;

public interface RequestLog {
	public void handleMissingItems(Map<ItemIdentifier,Integer> items);
	public void handleSucessfullRequestOf(ItemIdentifier item, int count);
	public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items);
}
