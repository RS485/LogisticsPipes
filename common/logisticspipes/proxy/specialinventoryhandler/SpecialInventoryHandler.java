package logisticspipes.proxy.specialinventoryhandler;

import java.util.Map;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.transactor.ITransactor;
import network.rs485.logisticspipes.inventory.ProviderMode;

public abstract class SpecialInventoryHandler implements IInventoryUtil, ITransactor {

	@Override
	public int itemCount(@Nonnull ItemIdentifier itemIdent) {
		final Map<ItemIdentifier, Integer> map = getItemsAndCount();
		return map.getOrDefault(itemIdent, 0);
	}

	@Override
	@Nonnull
	public ItemStack getMultipleItems(@Nonnull ItemIdentifier itemIdent, int count) {
		if (itemCount(itemIdent) < count) {
			return ItemStack.EMPTY;
		}
		return IntStream.range(0, count).mapToObj((i) -> getSingleItem(itemIdent)).filter(itemStack -> !itemStack.isEmpty()).reduce((left, right) -> {
			left.grow(right.getCount());
			return left;
		}).orElse(ItemStack.EMPTY);
	}

	public interface Factory {

		boolean init();

		boolean isType(@Nonnull TileEntity tile, @Nullable EnumFacing dir);

		@Nullable
		SpecialInventoryHandler getUtilForTile(@Nonnull TileEntity tile, @Nullable EnumFacing direction, @Nonnull ProviderMode mode);
	}
}
