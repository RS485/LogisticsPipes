package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IOrderManagerContentReceiver {

	void setOrderManagerContent(Collection<ItemIdentifierStack> _allItems);
}
