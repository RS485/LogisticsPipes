package logisticspipes.utils.transactor;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

public interface ITransactor {

	ItemStack add(ItemStack stack, ForgeDirection orientation, boolean doAdd);
}
