package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IChestContentReceiver {

	void setReceivedChestContent(Collection<ItemIdentifierStack> _allItems);

}
