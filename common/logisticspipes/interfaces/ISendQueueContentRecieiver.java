package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface ISendQueueContentRecieiver {

	public void handleSendQueueItemIdentifierList(Collection<ItemIdentifierStack> _allItems);
}
