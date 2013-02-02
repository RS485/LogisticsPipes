package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.utils.ItemIdentifierStack;

public interface ISendQueueContentRecieiver {
	public void handleSendQueueItemIdentifierList(List<ItemIdentifierStack> _allItems);
}
