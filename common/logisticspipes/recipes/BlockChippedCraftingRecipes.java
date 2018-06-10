package logisticspipes.recipes;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;

public class BlockChippedCraftingRecipes extends CraftingPartRecipes {
	@Override
	protected void loadRecipes(CraftingParts parts) {
		ItemStack logisticsBlockFrame = new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_BLOCK_FRAME.getMeta());

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_POWER_JUNCTION.getMeta()),
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

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_SECURITY_STATION.getMeta()),
				new RecipeManager.RecipeLayout(
						" g ",
						"rfr",
						"iri"
				),
				new RecipeManager.RecipeIndex('g', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('i', "ingotIron"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_STATISTICS_TABLE.getMeta()),
				new RecipeManager.RecipeLayout(
						" g ",
						"rfr",
						" i "
				),
				new RecipeManager.RecipeIndex('g', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('i', "ingotIron"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_FUZZYCRAFTING_TABLE.getMeta()),
				new RecipeManager.RecipeLayoutSmall(
						"c",
						"t"
				),
				new RecipeManager.RecipeIndex('c', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('t', new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_AUTOCRAFTING_TABLE.getMeta())));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.logisticsRequestTable),
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

	}

	@Override
	protected void loadPlainRecipes() {
	}
}
