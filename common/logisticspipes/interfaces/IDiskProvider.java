package logisticspipes.interfaces;

import logisticspipes.utils.gui.ItemDisplay;

import net.minecraft.item.ItemStack;

public interface IDiskProvider {

	ItemStack getDisk();

	int getX();

	int getY();

	int getZ();

	ItemDisplay getItemDisplay();
}
