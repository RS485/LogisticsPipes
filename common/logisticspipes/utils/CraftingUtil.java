package logisticspipes.utils;

import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

public class CraftingUtil {

	@SuppressWarnings("unchecked")
	// Suppressed because getRecipeList shouldn't ever return something that
	// isn't a recipe.
	public static List<IRecipe> getRecipeList() {
		return CraftingManager.getInstance().getRecipeList();
	}

}
