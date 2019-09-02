package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IModuleInventoryReceive {

	void handleInvContent(Collection<ItemIdentifierStack> _allItems);
}
