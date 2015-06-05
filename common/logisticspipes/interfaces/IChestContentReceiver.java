package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IChestContentReceiver {

	public void setReceivedChestContent(Collection<ItemIdentifierStack> _allItems);

}
