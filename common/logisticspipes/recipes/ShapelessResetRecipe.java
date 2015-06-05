package logisticspipes.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class ShapelessResetRecipe implements IRecipe {

	private final Item item;
	private final int meta;
	private final ItemStack output;

	public ShapelessResetRecipe(Item item, int meta) {
		output = new ItemStack(item, 1, meta);
		this.item = item;
		this.meta = meta;
	}

	@Override
	public boolean matches(InventoryCrafting var1, World var2) {
		int nmatches = 0;
		for (int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack slot = var1.getStackInSlot(i);
			if (slot == null) {
				continue;
			}
			if (slot.getItem() != item || slot.getItemDamage() != meta) {
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
			if (var1.getStackInSlot(i) == null) {
				continue;
			}
			nmatches++;
		}
		return new ItemStack(item, nmatches, meta);
	}

	@Override
	public int getRecipeSize() {
		return 1;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return output;
	}
}
