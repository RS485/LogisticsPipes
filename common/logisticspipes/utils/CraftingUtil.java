package logisticspipes.utils;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

public class CraftingUtil {

	// Suppressed because getRecipeList shouldn't ever return something that
	// isn't a recipe.
	public static RegistryNamespaced<ResourceLocation, IRecipe> getRecipeList() {
		return CraftingManager.REGISTRY;
	}

}
