package appeng.api;

import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Lets you manipulate Grinder Recipes, by adding or editing existing ones.
 */
public interface IGrinderRecipeManager {
	public List<IAppEngGrinderRecipe> getRecipes();

	public void addRecipe( ItemStack in, ItemStack out, int cost );

	public IAppEngGrinderRecipe getRecipeForInput(ItemStack input);
}
