package logisticspipes.interfaces;

import java.util.LinkedList;

import logisticspipes.utils.ItemIdentifierStack;

public interface ISendQueueContentRecieiver {
	public void handleSendQueueItemIdentifierList(LinkedList<ItemIdentifierStack> _allItems);
}
