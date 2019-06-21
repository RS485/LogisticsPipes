package logisticspipes.utils.transactor;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;

public abstract class Transactor implements ITransactor {

	@Nonnull
	@Override
	public ItemStack add(@Nonnull ItemStack stack, EnumFacing orientation, boolean doAdd) {
		ItemStack added = stack.copy();
		added.setCount(inject(stack, orientation, doAdd));
		return added;
	}

	public abstract int inject(ItemStack stack, EnumFacing orientation, boolean doAdd);
}
