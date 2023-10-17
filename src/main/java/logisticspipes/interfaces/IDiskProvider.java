package logisticspipes.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import logisticspipes.utils.gui.ItemDisplay;

public interface IDiskProvider {

	@Nonnull
	ItemStack getDisk();

	int getX();

	int getY();

	int getZ();

	ItemDisplay getItemDisplay();
}
