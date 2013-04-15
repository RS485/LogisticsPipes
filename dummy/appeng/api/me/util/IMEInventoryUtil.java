package appeng.api.me.util;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import appeng.api.IAEItemStack;

public interface IMEInventoryUtil {
	
	// ignores meta data
	public long getItemCountByType( IAEItemStack is );
	
	// ignores meta data - is always Fuzzy...
	// will only remove single items, this ignores your stack size...
	public ItemStack extractItemsByRecipe(World w, IRecipe r, InventoryCrafting ci, ItemStack providedTemplate, int slot);
	
	// ignores meta data - is always Fuzzy...
	//public ItemStack extractItemsByType( ItemStack is ); // will only remove single items, this ignores your stack size...
	
}
