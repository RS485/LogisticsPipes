package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemStack;

public interface IModuleInventoryReceive {

	void handleInvContent(Collection<ItemStack> _allItems);
}
