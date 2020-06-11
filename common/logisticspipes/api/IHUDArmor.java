package logisticspipes.api;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface IHUDArmor {

	boolean isEnabled(@Nonnull ItemStack item);
}
