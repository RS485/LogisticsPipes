package logisticspipes.interfaces;

import net.minecraft.item.ItemStack;

public interface IItemAdvancedExistance {

	public boolean canExistInNormalInventory(ItemStack stack);

	public boolean canExistInWorld(ItemStack stack);
}
