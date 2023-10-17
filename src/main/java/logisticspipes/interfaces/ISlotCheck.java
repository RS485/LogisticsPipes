package logisticspipes.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface ISlotCheck {

	boolean isStackAllowed(@Nonnull ItemStack itemStack);

}
