package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

public class ShapelessResetRecipe extends ShapelessRecipes {
	private final int itemID;
	private final int meta;

	public ShapelessResetRecipe(int itemID, int meta) {
		super(new ItemStack(itemID, 1, meta), new ArrayList<ItemStack>(1));
		recipeItems.add(super.getRecipeOutput());
		this.itemID = itemID;
		this.meta = meta;
	}

	@Override
	public boolean matches(InventoryCrafting var1, World var2) {
		int nmatches = 0;
		for (int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack slot = var1.getStackInSlot(i);
			if(slot == null)
				continue;
			if(slot.itemID != itemID || slot.getItemDamage() != meta) {
				return false;
			}
			nmatches++;
		}
		return (nmatches > 0);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		int nmatches = 0;
		for (int i = 0; i < var1.getSizeInventory(); i++) {
			if(var1.getStackInSlot(i) == null)
				continue;
			nmatches++;
		}
		return new ItemStack(itemID, nmatches, meta);
	}

	@Override
	public int getRecipeSize() {
		return 1;
	}
}
