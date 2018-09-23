package logisticspipes.proxy.specialinventoryhandler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.misc.CapUtil;

import logisticspipes.utils.item.ItemIdentifier;

public class BuildCraftTransactorHandler extends SpecialInventoryHandler {

	private IItemTransactor cap = null;

	private BuildCraftTransactorHandler(IItemTransactor cap) {
		this.cap = cap;
	}

	public BuildCraftTransactorHandler() {
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile, EnumFacing dir) {
		return tile.hasCapability(CapUtil.CAP_ITEM_TRANSACTOR, dir);
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		IItemTransactor cap = tile.getCapability(CapUtil.CAP_ITEM_TRANSACTOR, dir);
		if (cap != null) {
			return new BuildCraftTransactorHandler(cap);
		}
		return null;
	}

	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return Collections.emptyMap();
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier item) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier item) {
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}

	@Override
	public int roomForItem(ItemIdentifier item, int count) {
		return cap.insert(item.makeNormalStack(64), false, true).getCount();
	}

	@Override
	public boolean isSpecialInventory() {
		return true;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		return Collections.emptySet();
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot != 0) return ItemStack.EMPTY;
		return cap.extract(it -> true, 0, 64, true);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if (slot != 0) return ItemStack.EMPTY;
		return cap.extract(it -> true, amount, amount, false);
	}

	@Override
	public ItemStack add(ItemStack stack, EnumFacing orientation, boolean doAdd) {
		ItemStack overflow = cap.insert(stack.copy(), false, !doAdd);
		stack.setCount(stack.getCount() - overflow.getCount());
		return stack;
	}
}
