package logisticspipes.proxy.bs;

import net.minecraft.item.ItemStack;

public interface ICrateStorageProxy {

	Iterable<ItemStack> getContents();

	int getUniqueItems();

	int getItemCount(ItemStack stack);

	ItemStack extractItems(ItemStack stack, int count);

	int getSpaceForItem(ItemStack stack);

	ItemStack insertItems(ItemStack stack);
}
