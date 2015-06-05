package logisticspipes.utils.transactor;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

public abstract class Transactor implements ITransactor {

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection orientation, boolean doAdd) {
		ItemStack added = stack.copy();
		added.stackSize = inject(stack, orientation, doAdd);
		return added;
	}

	public abstract int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd);
}
