package logisticspipes.interfaces;

import java.util.Collection;
import java.util.List;

import logisticspipes.utils.ItemIdentifierStack;

public interface ISendQueueContentRecieiver {
	public void handleSendQueueItemIdentifierList(Collection<ItemIdentifierStack> _allItems);
}
