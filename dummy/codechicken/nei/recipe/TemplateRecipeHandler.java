package codechicken.nei.recipe;

import java.util.ArrayList;

import net.minecraft.src.ItemStack;

public abstract class TemplateRecipeHandler implements ICraftingHandler, IUsageHandler {
	public abstract class CachedRecipe {}
	public ArrayList<CachedRecipe> arecipes = new ArrayList<CachedRecipe>();
	public void loadUsageRecipes(ItemStack ingredient) {}
	public void loadCraftingRecipes(String outputId, Object... results) {}
}
