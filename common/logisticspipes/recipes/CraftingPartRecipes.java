package logisticspipes.recipes;

import logisticspipes.LPItems;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

				SolderingStationRecipes.loadRecipe(parts);
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
		loadPlainRecipes();
		getCraftingPartList().forEach(this::loadRecipes);
	}

	protected abstract void loadRecipes(CraftingParts parts);

	protected abstract void loadPlainRecipes();
}
