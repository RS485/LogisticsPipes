package logisticspipes.request;

import java.util.LinkedList;

import logisticspipes.main.ItemMessage;

public interface RequestLog {
	public void handleMissingItems(LinkedList<ItemMessage> list);
	public void handleSucessfullRequestOf(ItemMessage item);
	public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items);
}
