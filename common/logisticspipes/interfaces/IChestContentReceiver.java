package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.ItemIdentifierStack;

public interface IChestContentReceiver {
	
	public void setReceivedChestContent(Collection<ItemIdentifierStack> _allItems);
	
}
