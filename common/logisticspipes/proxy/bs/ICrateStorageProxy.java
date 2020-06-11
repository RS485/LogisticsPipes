package logisticspipes.proxy.bs;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface ICrateStorageProxy {

	Iterable<ItemStack> getContents();

	int getUniqueItems();

	int getItemCount(@Nonnull ItemStack stack);

	@Nonnull
	ItemStack extractItems(@Nonnull ItemStack stack, int count);

	int getSpaceForItem(@Nonnull ItemStack stack);

	@Nonnull
	ItemStack insertItems(@Nonnull ItemStack stack);
}
