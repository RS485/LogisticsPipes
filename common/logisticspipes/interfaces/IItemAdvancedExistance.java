package logisticspipes.interfaces;

import net.minecraft.item.ItemStack;

public interface IItemAdvancedExistance {

	boolean canExistInNormalInventory(ItemStack stack);

	boolean canExistInWorld(ItemStack stack);
}
