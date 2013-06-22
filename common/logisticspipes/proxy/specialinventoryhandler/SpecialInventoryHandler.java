package logisticspipes.proxy.specialinventoryhandler;

import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.filters.IStackFilter;

public abstract class SpecialInventoryHandler implements IInventoryUtil, ITransactor {
	public abstract boolean init();
	public abstract boolean isType(TileEntity tile);
	public abstract SpecialInventoryHandler getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd);

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		Map<ItemIdentifier, Integer> map = getItemsAndCount();
		Integer count = map.get(itemIdent);
		if(count==null) {
			return 0;
		}
		return count;
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
	public ItemStack remove(IStackFilter filter, ForgeDirection orientation, boolean doRemove) {
		//Currently unimplemented because LP has it's own methods
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException();
		}
		return null;
	}
}
