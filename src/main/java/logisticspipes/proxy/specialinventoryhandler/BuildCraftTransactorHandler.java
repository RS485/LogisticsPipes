package logisticspipes.proxy.specialinventoryhandler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.misc.CapUtil;

import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.inventory.ProviderMode;

public class BuildCraftTransactorHandler extends SpecialInventoryHandler implements SpecialInventoryHandler.Factory {

	private IItemTransactor cap = null;

	private BuildCraftTransactorHandler(IItemTransactor cap) {
		this.cap = cap;
	}

	public BuildCraftTransactorHandler() {}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(@Nonnull TileEntity tile, @Nullable EnumFacing dir) {
		return tile.hasCapability(CapUtil.CAP_ITEM_TRANSACTOR, dir);
	}

	@Nullable
	@Override
	public SpecialInventoryHandler getUtilForTile(@Nonnull TileEntity tile, @Nullable EnumFacing direction, @Nonnull ProviderMode mode) {
		IItemTransactor cap = tile.getCapability(CapUtil.CAP_ITEM_TRANSACTOR, direction);
		if (cap != null) {
			return new BuildCraftTransactorHandler(cap);
		}
		return null;
	}

	@Override
	@Nonnull
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return Collections.emptyMap();
	}

	@Override
	@Nonnull
	public ItemStack getSingleItem(ItemIdentifier item) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean containsUndamagedItem(@Nonnull ItemIdentifier item) {
		return false;
	}

	@Override
	public int roomForItem(@Nonnull ItemStack stack) {
		return stack.getCount() - cap.insert(stack, false, true).getCount();
	}

	@Override
	@Nonnull
	public Set<ItemIdentifier> getItems() {
		return Collections.emptySet();
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		if (slot != 0) return ItemStack.EMPTY;
		return cap.extract(it -> true, 0, 64, true);
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int slot, int amount) {
		if (slot != 0) return ItemStack.EMPTY;
		return cap.extract(it -> true, amount, amount, false);
	}

	@Override
	@Nonnull
	public ItemStack add(@Nonnull ItemStack stack, EnumFacing orientation, boolean doAdd) {
		ItemStack overflow = cap.insert(stack.copy(), false, !doAdd);
		stack.setCount(stack.getCount() - overflow.getCount());
		return stack;
	}
}
