package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemStack;

public interface IOrderManagerContentReceiver {

	void setOrderManagerContent(Collection<ItemStack> _allItems);
}
