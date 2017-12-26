package logisticspipes.utils;

import java.util.Iterator;
import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

public class CraftingUtil {

	@SuppressWarnings("unchecked")
	// Suppressed because getRecipeList shouldn't ever return something that
	// isn't a recipe.
	public static RegistryNamespaced<ResourceLocation, IRecipe> getRecipeList() {
		return CraftingManager.REGISTRY;
	}

}
