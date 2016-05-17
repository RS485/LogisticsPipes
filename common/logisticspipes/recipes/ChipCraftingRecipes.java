package logisticspipes.recipes;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;

public class ChipCraftingRecipes extends CraftingPartRecipes {
	@Override
	protected void loadRecipes(CraftingParts parts) {
		ItemStack logisticsBlockFrame = new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_BLOCK_FRAME);

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_POWER_JUNCTION), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" c ",
						"rfr",
						"ibi"
				),
				new RecipeManager.RecipeIndex('c', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('b', Blocks.redstone_block));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_SECURITY_STATION), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" g ",
						"rfr",
						"iri"
				),
				new RecipeManager.RecipeIndex('g', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('i', "ingotIron"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_STATISTICS_TABLE), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" g ",
						"rfr",
						" i "
				),
				new RecipeManager.RecipeIndex('g', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('i', "ingotIron"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_FUZZYCRAFTING_TABLE), CraftingDependency.Basic,
				new RecipeManager.RecipeLayoutSmall(
						"c",
						"t"
				),
				new RecipeManager.RecipeIndex('c', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('t', new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE)));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.logisticsRequestTable), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" c ",
						"rfp",
						" k "
				),
				new RecipeManager.RecipeIndex('c', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', LogisticsPipes.LogisticsRequestPipeMk2),
				new RecipeManager.RecipeIndex('f', new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_BLOCK_FRAME)),
				new RecipeManager.RecipeIndex('k', Blocks.chest),
				new RecipeManager.RecipeIndex('p', LogisticsPipes.LogisticsCraftingPipeMk1));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe), CraftingDependency.Basic,
				new RecipeManager.RecipeLayoutSmall(
						"f",
						"p"
				),
				new RecipeManager.RecipeIndex('f', parts.getChipFpga()),
				new RecipeManager.RecipeIndex('p', new ItemStack(LogisticsPipes.BasicTransportPipe)));
	}

	@Override
	protected void loadPlainRecipes() {
	}
}
