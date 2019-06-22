package logisticspipes.proxy.specialinventoryhandler;

import java.util.Map;
import java.util.stream.IntStream;

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
		final Map<ItemIdentifier, Integer> map = getItemsAndCount();
		return map.getOrDefault(itemIdent, 0);
	}

	@Override
	public @Nonnull ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		if (itemCount(itemIdent) < count) {
			return ItemStack.EMPTY;
		}
		return IntStream.range(0, count)
				.mapToObj((i) -> getSingleItem(itemIdent))
				.filter(itemStack -> !itemStack.isEmpty())
				.reduce((left, right) -> {
					left.grow(right.getCount());
					return left;
				}).orElse(ItemStack.EMPTY);
	}
}
