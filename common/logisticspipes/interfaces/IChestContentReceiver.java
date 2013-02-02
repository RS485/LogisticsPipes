package logisticspipes.interfaces;

import java.util.LinkedList;

import logisticspipes.utils.ItemIdentifierStack;

public interface IChestContentReceiver {
	
	public void setReceivedChestContent(LinkedList<ItemIdentifierStack> list);
	
}
