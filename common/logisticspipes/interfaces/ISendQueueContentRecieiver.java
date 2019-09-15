package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemStack;

public interface ISendQueueContentRecieiver {

	void handleSendQueueItemIdentifierList(Collection<ItemStack> _allItems);
}
