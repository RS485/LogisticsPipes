package logisticspipes.interfaces;

import java.util.Map;
import java.util.Set;

import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.item.ItemStack;

public interface IInventoryUtil {

	public int itemCount(ItemIdentifier item);

	public Map<ItemIdentifier, Integer> getItemsAndCount();

	public ItemStack getSingleItem(ItemIdentifier item);

	public ItemStack getMultipleItems(ItemIdentifier item, int count);

	/**
	 * Checks to see if the item is inside the inventory. Used by the PolymorphicItemSink
	 * This includes slots that are limited to one item type but don't contain any items.
	 * @param item The item to check
	 * @return true if the item is inside the inventory
	 */
	public boolean containsUndamagedItem(ItemIdentifier item);

	public int roomForItem(ItemIdentifier item);

	/**
	 * Inventory space count which terminates when space for max items are
	 * found.
	 * 
	 * @param item
	 * @param max
	 * @return spaces found. If this is less than max, then there are only
	 *         spaces for that amount.
	 */
	public int roomForItem(ItemIdentifier item, int count);

	public boolean isSpecialInventory();

	Set<ItemIdentifier> getItems();

	//IInventory adapter
	public int getSizeInventory();

	public ItemStack getStackInSlot(int i);

	public ItemStack decrStackSize(int i, int j);
}
