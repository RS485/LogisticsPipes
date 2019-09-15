package logisticspipes.interfaces;

import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import network.rs485.logisticspipes.util.ItemVariant;

public interface WrappedInventory {

	int itemCount(ItemVariant item);

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
	int getSizeInventory();

	@Nonnull
	ItemStack getStackInSlot(int slot);

	@Nonnull
	ItemStack decrStackSize(int slot, int amount);
}
