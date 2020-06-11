package logisticspipes.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface ISlotClick {

	@Nonnull
	ItemStack getResultForClick();
}
