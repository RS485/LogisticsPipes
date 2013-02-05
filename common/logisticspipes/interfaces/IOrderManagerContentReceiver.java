package logisticspipes.interfaces;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import logisticspipes.utils.ItemIdentifierStack;

public interface IOrderManagerContentReceiver {
	public void setOrderManagerContent(Collection<ItemIdentifierStack> _allItems);
}
