package logisticspipes.interfaces;

import java.util.Collection;
import java.util.List;

import logisticspipes.utils.ItemIdentifierStack;

public interface IModuleInventoryReceive {
	public void handleInvContent(Collection<ItemIdentifierStack> _allItems);
}
