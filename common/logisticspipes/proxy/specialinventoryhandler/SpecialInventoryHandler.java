package logisticspipes.proxy.specialinventoryhandler;

import java.util.Map;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.transactor.ITransactor;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public abstract class SpecialInventoryHandler implements IInventoryUtil, ITransactor {

	public abstract boolean init();

	public abstract boolean isType(TileEntity tile);

	public abstract SpecialInventoryHandler getUtilForTile(TileEntity tile, ForgeDirection dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd);

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		Map<ItemIdentifier, Integer> map = getItemsAndCount();
		Integer count = map.get(itemIdent);
		if (count == null) {
			return 0;
		}
		return count;
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		if (itemCount(itemIdent) < count) {
			return null;
		}
		ItemStack stack = null;
		for (int i = 0; i < count; i++) {
			if (stack == null) {
				stack = getSingleItem(itemIdent);
			} else {
				ItemStack newstack = getSingleItem(itemIdent);
				if (newstack == null) {
					break;
				}
				stack.stackSize += newstack.stackSize;
			}
		}
		return stack;
	}
}
