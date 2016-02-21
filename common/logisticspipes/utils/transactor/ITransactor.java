package logisticspipes.utils.transactor;

import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;

public interface ITransactor {

	ItemStack add(ItemStack stack, EnumFacing orientation, boolean doAdd);
}
