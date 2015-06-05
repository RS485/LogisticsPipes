package logisticspipes.interfaces;

import logisticspipes.utils.gui.ItemDisplay;

import net.minecraft.item.ItemStack;

public interface IDiskProvider {

	public ItemStack getDisk();

	public int getX();

	public int getY();

	public int getZ();

	public ItemDisplay getItemDisplay();
}
