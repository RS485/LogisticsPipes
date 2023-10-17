package logisticspipes.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface ISpecialInsertion {

	int addToSlot(@Nonnull ItemStack stack, int i);
}
