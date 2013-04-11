package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.ItemIdentifierStack;

public interface IModuleInventoryReceive {
	public void handleInvContent(Collection<ItemIdentifierStack> _allItems);
}
