package logisticspipes.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface ICraftingResultHandler {

	public void handleCrafting(@Nonnull ItemStack stack);
}
