package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.utils.item.ItemStack;

public interface IChestContentReceiver {

	void setReceivedChestContent(Collection<ItemStack> _allItems);

}
