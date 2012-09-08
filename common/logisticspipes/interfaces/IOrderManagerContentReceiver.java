package logisticspipes.interfaces;

import java.util.LinkedList;

import logisticspipes.utils.ItemIdentifierStack;

public interface IOrderManagerContentReceiver {
	public void setOrderManagerContent(LinkedList<ItemIdentifierStack> list);
}
