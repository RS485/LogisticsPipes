package logisticspipes.interfaces;

import java.util.LinkedList;

import logisticspipes.utils.ItemIdentifierStack;

public interface IModuleInventoryReceive {
	public void handleInvContent(LinkedList<ItemIdentifierStack> list);
}
