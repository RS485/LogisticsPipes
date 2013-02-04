package logisticspipes.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class ShapelessResetRecipe implements IRecipe {
	private final int itemID;
	private final int meta;
	private final ItemStack output;

	public ShapelessResetRecipe(int itemID, int meta) {
		this.output = new ItemStack(itemID, 1, meta);
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

	@Override
	public ItemStack getRecipeOutput() {
		return this.output;
	}
}
