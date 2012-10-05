package logisticspipes.request;

import java.util.LinkedList;

import logisticspipes.utils.ItemMessage;

public interface RequestLog {
	public void handleMissingItems(LinkedList<ItemMessage> list);
	public void handleSucessfullRequestOf(ItemMessage item);
	public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items);
}
