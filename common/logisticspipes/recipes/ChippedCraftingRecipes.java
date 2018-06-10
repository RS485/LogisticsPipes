package logisticspipes.recipes;

import net.minecraft.item.ItemStack;

import logisticspipes.LogisticsPipes;

public class ChippedCraftingRecipes extends CraftingPartRecipes {
	@Override
	protected void loadRecipes(CraftingParts parts) {
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProgrammer),
				new RecipeManager.RecipeLayout(
						" a ",
						"ifi",
						"gmg"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('f', parts.getChipFpga()),
				new RecipeManager.RecipeIndex('g', "ingotGold"),
				new RecipeManager.RecipeIndex('m', new ItemStack(LogisticsPipes.LogisticsBlankModule, 1)));

	}

	@Override
	protected void loadPlainRecipes() {
	}
}
