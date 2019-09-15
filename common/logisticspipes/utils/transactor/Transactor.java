package logisticspipes.utils.transactor;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public abstract class Transactor implements ITransactor {

	@Override
	public ItemStack add(ItemStack stack, Direction orientation, boolean doAdd) {
		ItemStack added = stack.copy();
		added.setCount(inject(stack, orientation, doAdd));
		return added;
	}

	public abstract int inject(ItemStack stack, Direction orientation, boolean doAdd);
}
