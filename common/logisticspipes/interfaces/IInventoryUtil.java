package logisticspipes.interfaces;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import logisticspipes.utils.item.ItemIdentifier;

public interface IInventoryUtil {

	int itemCount(ItemIdentifier item);

	Map<ItemIdentifier, Integer> getItemsAndCount();

	@Nonnull
	ItemStack getSingleItem(ItemIdentifier item);

	@Nonnull
	ItemStack getMultipleItems(ItemIdentifier item, int count);

	/**
	 * Checks to see if the item is inside the inventory. Used by the PolymorphicItemSink
	 * This includes slots that are limited to one item type but don't contain any items.
	 *
	 * @param item The item to check
	 * @return true if the item is inside the inventory
	 */
	boolean containsUndamagedItem(ItemIdentifier item);

	/**
	 * Checks all possible room for a given item. This method is laggy!
	 * @see #roomForItem(ItemIdentifier, int)
	 * @deprecated use {@link #roomForItem(ItemStack)} where possible
	 */
	@Deprecated
	int roomForItem(ItemIdentifier item);

	/**
	 * @deprecated use {@link #roomForItem(ItemStack)} where possible
	 */
	@Deprecated
	int roomForItem(ItemIdentifier item, int count);

	/**
	 * Inventory space count which terminates when space for max items are
	 * found.
	 *
	 * @return spaces found. If this is less than max, then there are only
	 * spaces for that amount.
	 */
	int roomForItem(ItemStack stack);

	Set<ItemIdentifier> getItems();

	//IInventory adapter
	int getSizeInventory();

	@Nonnull
	ItemStack getStackInSlot(int slot);

	@Nonnull
	ItemStack decrStackSize(int slot, int amount);
}
