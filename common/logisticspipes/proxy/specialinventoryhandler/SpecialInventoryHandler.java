package logisticspipes.proxy.specialinventoryhandler;

import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.utils.transactor.ITransactor;
import network.rs485.logisticspipes.util.ItemVariant;

public abstract class SpecialInventoryHandler implements WrappedInventory, ITransactor {

	@Override
	@Nonnull
	public ItemStack getMultipleItems(ItemStack stack) {
		final int count = stack.getCount();
		final ItemVariant variant = ItemVariant.fromStack(stack);

		if (itemCount(variant) < count) return ItemStack.EMPTY;

		return IntStream.range(0, count)
				.mapToObj((i) -> getSingleItem(variant))
				.filter(itemStack -> !itemStack.isEmpty())
				.reduce((left, right) -> {
					left.increment(right.getCount());
					return left;
				}).orElse(ItemStack.EMPTY);
	}
}
