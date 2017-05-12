package logisticspipes.recipes;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;

public class ChipCraftingRecipes extends CraftingPartRecipes {
	@Override
	protected void loadRecipes(CraftingParts parts) {
		ItemStack logisticsBlockFrame = new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_BLOCK_FRAME.getMeta());

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_POWER_JUNCTION.getMeta()), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" c ",
						"rfr",
						"ibi"
				),
				new RecipeManager.RecipeIndex('c', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('b', Blocks.REDSTONE_BLOCK));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_SECURITY_STATION.getMeta()), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" g ",
						"rfr",
						"iri"
				),
				new RecipeManager.RecipeIndex('g', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('i', "ingotIron"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_STATISTICS_TABLE.getMeta()), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" g ",
						"rfr",
						" i "
				),
				new RecipeManager.RecipeIndex('g', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('i', "ingotIron"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_FUZZYCRAFTING_TABLE.getMeta()), CraftingDependency.Basic,
				new RecipeManager.RecipeLayoutSmall(
						"c",
						"t"
				),
				new RecipeManager.RecipeIndex('c', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('t', new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_AUTOCRAFTING_TABLE.getMeta())));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.logisticsRequestTable), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" c ",
						"rfp",
						" k "
				),
				new RecipeManager.RecipeIndex('c', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', LogisticsPipes.LogisticsRequestPipeMk2),
				new RecipeManager.RecipeIndex('f', new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_BLOCK_FRAME.getMeta())),
				new RecipeManager.RecipeIndex('k', Blocks.CHEST),
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
