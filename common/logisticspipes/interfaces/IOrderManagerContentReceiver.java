package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IOrderManagerContentReceiver {

	public void setOrderManagerContent(Collection<ItemIdentifierStack> _allItems);
}
