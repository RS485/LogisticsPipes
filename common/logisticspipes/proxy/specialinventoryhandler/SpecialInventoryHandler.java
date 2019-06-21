package logisticspipes.proxy.specialinventoryhandler;

import java.util.Map;

import javax.annotation.Nonnull;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.transactor.ITransactor;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

public abstract class SpecialInventoryHandler implements IInventoryUtil, ITransactor {

	public abstract boolean init();

	public abstract boolean isType(TileEntity tile, EnumFacing dir);

	public abstract SpecialInventoryHandler getUtilForTile(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd);

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		Map<ItemIdentifier, Integer> map = getItemsAndCount();
		Integer count = map.get(itemIdent);
		if (count == null) {
			return 0;
		}
		return count;
	}

	@Nonnull
	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		if (itemCount(itemIdent) < count) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = ItemStack.EMPTY;
		for (int i = 0; i < count; i++) {
			if (stack.isEmpty()) {
				stack = getSingleItem(itemIdent);
			} else {
				ItemStack newstack = getSingleItem(itemIdent);
				if (newstack.isEmpty()) {
					break;
				}
				stack.grow(newstack.getCount());
			}
		}
		return stack;
	}
}
