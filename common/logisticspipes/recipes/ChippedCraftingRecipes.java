package logisticspipes.recipes;

import net.minecraft.init.Items;
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

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer),
				new RecipeManager.RecipeLayout(
						"aga",
						"rsl",
						"rrl"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('g', "ingotGold"),
				new RecipeManager.RecipeIndex('s', "blockGlass"),
				new RecipeManager.RecipeIndex('l', "gemLapis"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeControllerItem),
				new RecipeManager.RecipeLayout(
						"gbg",
						"rsl",
						"rrl"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('g', "ingotGold"),
				new RecipeManager.RecipeIndex('s', "blockGlass"),
				new RecipeManager.RecipeIndex('l', "gemLapis"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingSignCreator),
				new RecipeManager.RecipeLayout(
						"b b",
						" s ",
						" a "
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('s', Items.SIGN));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeManagerItem),
				new RecipeManager.RecipeLayout(
						"ibi",
						"rsl",
						"rrl"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('s', "blockGlass"),
				new RecipeManager.RecipeIndex('l', "gemLapis"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"));

	}

	@Override
	protected void loadPlainRecipes() {
	}
}
