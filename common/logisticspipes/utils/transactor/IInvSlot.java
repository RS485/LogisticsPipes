package logisticspipes.utils.transactor;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface IInvSlot {

	boolean canPutStackInSlot(@Nonnull ItemStack stack);

	@Nonnull
	ItemStack getStackInSlot();

	@Nonnull
	ItemStack insertItem(@Nonnull ItemStack stack, boolean simulate);

	@Nonnull
	ItemStack extractItem(int amount, boolean simulate);

	int getSlotLimit();
}
