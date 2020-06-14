package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import logisticspipes.LPItems;

public abstract class CraftingPartRecipes implements IRecipeProvider {

	private static List<CraftingParts> craftingPartList = null;

	public static List<CraftingParts> getCraftingPartList() {
		if (craftingPartList == null) {
			craftingPartList = new ArrayList<>();
			/*
			CraftingParts parts = SimpleServiceLocator.buildCraftProxy.getRecipeParts();
			// NO BC => NO RECIPES (for now)
			if (parts != null) {
				SimpleServiceLocator.IC2Proxy.addCraftingRecipes(parts);
				SimpleServiceLocator.thaumCraftProxy.addCraftingRecipes(parts);
				SimpleServiceLocator.ccProxy.addCraftingRecipes(parts);
				SimpleServiceLocator.buildCraftProxy.addCraftingRecipes(parts);

				RecipeManager.loadRecipes();
			}
			*/

			if (true) { // TODO: Add Config Option
				craftingPartList.add(new CraftingParts(
						new ItemStack(LPItems.chipFPGA, 1),
						new ItemStack(LPItems.chipBasic, 1),
						new ItemStack(LPItems.chipAdvanced, 1)));
			}
		}

		return craftingPartList;
	}

	@Override
	public final void loadRecipes() {
		getCraftingPartList().forEach(this::loadRecipes);
	}

	protected abstract void loadRecipes(CraftingParts parts);

}
