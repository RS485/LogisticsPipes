package logisticspipes.recipes;

import logisticspipes.LPItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ChippedCraftingRecipes extends CraftingPartRecipes {
	@Override
	protected void loadRecipes(CraftingParts parts) {
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.logisticsProgrammer),
				new RecipeManager.RecipeLayout(
						" a ",
						"ifi",
						"gmg"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('f', parts.getChipFpga()),
				new RecipeManager.RecipeIndex('g', "ingotGold"),
				new RecipeManager.RecipeIndex('m', new ItemStack(LPItems.blankModule, 1)));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.remoteOrderer),
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

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeController),
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

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.signCreator),
				new RecipeManager.RecipeLayout(
						"b b",
						" s ",
						" a "
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('s', Items.SIGN));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeManager),
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
