package logisticspipes.interfaces;

import java.util.Map;
import java.util.Set;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;

public interface IInventoryUtil {
	public int itemCount(ItemIdentifier item);
	public Map<ItemIdentifier, Integer> getItemsAndCount();
	public ItemStack getSingleItem(ItemIdentifier item);
	public ItemStack getMultipleItems(ItemIdentifier item, int count);
	public boolean containsItem(ItemIdentifier item);
	public boolean containsUndamagedItem(ItemIdentifier item);
	public int roomForItem(ItemIdentifier item);
	/**
	 * Inventory space count which terminates when space for max items are found.
	 * @param item
	 * @param max
	 * @return spaces found. 
	 * If this is less than max, then there are only spaces for that amount.
	 */
	public int roomForItem(ItemIdentifier item, int count);

	public boolean isSpecialInventory();
	
	Set<ItemIdentifier> getItems();
	
	//IInventory adapter
	public int getSizeInventory();
	public ItemStack getStackInSlot(int i);
	public ItemStack decrStackSize(int i, int j);
}
