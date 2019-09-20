package logisticspipes.interfaces;

import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import network.rs485.logisticspipes.util.ItemVariant;

public interface WrappedInventory {

	default int itemCount(ItemVariant variant) {
		final Set<ItemStack> stacks = getItemsAndCount();
		return stacks.stream().filter(variant::matches).mapToInt(ItemStack::getCount).sum();
	}

	Set<ItemStack> getItemsAndCount();

	@Nonnull
	ItemStack getSingleItem(ItemVariant item);

	@Nonnull
	ItemStack getMultipleItems(ItemStack stack);

	/**
	 * Checks to see if the item is inside the inventory. Used by the PolymorphicItemSink
	 * This includes slots that are limited to one item type but don't contain any items.
	 *
	 * @param item The item to check
	 * @return true if the item is inside the inventory
	 */
	boolean containsUndamagedItem(ItemVariant item);

	int roomForItem(ItemVariant item);

	/**
	 * Inventory space count which terminates when space for max items are
	 * found.
	 *
	 * @return spaces found. If this is less than max, then there are only
	 * spaces for that amount.
	 */
	int roomForItem(ItemStack item);

	Set<ItemVariant> getItems();

	// IInventory adapter
	int getSlotCount();

	@Nonnull
	ItemStack getInvStack(int slot);

	@Nonnull
	ItemStack takeInvStack(int slot, int amount);
}
