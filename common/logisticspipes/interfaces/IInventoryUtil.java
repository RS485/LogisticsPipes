package logisticspipes.interfaces;

import java.util.HashMap;
import java.util.Set;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;

public interface IInventoryUtil {
	public int itemCount(ItemIdentifier item);
	public HashMap<ItemIdentifier, Integer> getItemsAndCount();
	public ItemStack getSingleItem(ItemIdentifier item);
	public ItemStack getMultipleItems(ItemIdentifier item, int count);
	public boolean containsItem(ItemIdentifier item);
	public boolean containsUndamagedItem(ItemIdentifier item);
	public int roomForItem(ItemIdentifier item);
	public boolean hasRoomForItem(ItemIdentifier item);
	Set<ItemIdentifier> getItems();
}
