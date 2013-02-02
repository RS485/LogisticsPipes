package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.utils.ItemIdentifierStack;

public interface IModuleInventoryReceive {
	public void handleInvContent(List<ItemIdentifierStack> list);
}
