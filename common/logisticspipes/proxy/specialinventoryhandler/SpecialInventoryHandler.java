package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public abstract class SpecialInventoryHandler implements IInventoryUtil {
	public abstract boolean init();
	public abstract boolean isType(TileEntity tile);
	public abstract IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd);

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		HashMap<ItemIdentifier, Integer> map = getItemsAndCount();
		if(map.containsKey(itemIdent)) {
			return map.get(itemIdent);
		}
		return 0;
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		if (itemCount(itemIdent) < count) return null;
		ItemStack stack = null;
		for (int i = 0; i < count; i++) {
			if(stack == null) {
				stack = getSingleItem(itemIdent);
			} else {
				stack.stackSize += getSingleItem(itemIdent).stackSize;
			}
		}
		return stack;
	}

	@Override
	public boolean hasRoomForItem(ItemIdentifier itemIdent) {
		return roomForItem(itemIdent) > 0;
	}
}
