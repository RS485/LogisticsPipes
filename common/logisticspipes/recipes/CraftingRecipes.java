package logisticspipes.recipes;

import java.util.Map;

import logisticspipes.LPBlocks;
import logisticspipes.LPItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.items.ItemModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;

//@formatter:off
//CHECKSTYLE:OFF

public class CraftingRecipes implements IRecipeProvider {
	@Override
	public void loadRecipes() {
		String[] dyes =
				{
						"dyeBlack",
						"dyeRed",
						"dyeGreen",
						"dyeBrown",
						"dyeBlue",
						"dyePurple",
						"dyeCyan",
						"dyeLightGray",
						"dyeGray",
						"dyePink",
						"dyeLime",
						"dyeYellow",
						"dyeLightBlue",
						"dyeMagenta",
						"dyeOrange",
						"dyeWhite"
				};




		ItemStack logisticsBlockFrame = new ItemStack(LPBlocks.solidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_BLOCK_FRAME.getMeta());

		RecipeManager.craftingManager.addRecipe(logisticsBlockFrame,
				new RecipeManager.RecipeLayout(
						"iri",
						"   ",
						"w w"),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('w', "plankWood"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPBlocks.solidBlock, 1, LogisticsSolidBlock.BlockType.SOLDERING_STATION.getMeta()),
				new RecipeManager.RecipeLayout(
						" c ",
						"ifi",
						"ibi"
				),
				new RecipeManager.RecipeIndex('c', Blocks.CRAFTING_TABLE),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('b', Items.BLAZE_POWDER));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPBlocks.solidBlock, 1, LogisticsSolidBlock.BlockType.LOGISTICS_AUTOCRAFTING_TABLE.getMeta()),
				new RecipeManager.RecipeLayout(
						" c ",
						"wfw",
						" p "
				),
				new RecipeManager.RecipeIndex('c', Blocks.CRAFTING_TABLE),
				new RecipeManager.RecipeIndex('w', "plankWood"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('p', Blocks.PISTON));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeTransportBasic, 8),
				new RecipeManager.RecipeLayout(
						"iri",
						"g g",
						"iri"
				),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('g', "blockGlass"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.blankModule, 1),
				new RecipeManager.RecipeLayout(
						"r",
						"p",
						"g"
				),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('p', "paper"),
				new RecipeManager.RecipeIndex('g', "nuggetGold"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.disk, 1),
				new RecipeManager.RecipeLayout(
						"iri",
						"rgr",
						"iri"
				),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('g', "nuggetGold"));

		registerResetRecipe(dyes);
	}

	private void registerResetRecipe(String[] dyes) {
		for (Map.Entry<Class<? extends LogisticsModule>, ItemModule> entry : LPItems.modules.entrySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			LogisticsModule module = entry.getValue().getModuleForItem(new ItemStack(entry.getValue()), null, null, null);
			boolean force = false;
			try {
				module.writeToNBT(nbt);
			} catch (Exception e) {
				force = true;
			}
			if (!nbt.equals(new NBTTagCompound()) || force) {
				RecipeManager.craftingManager.addShapelessResetRecipe(entry.getValue(), 0);
			}
		}

		for (int i = 1; i < 17; i++) {
			RecipeManager.craftingManager.addOrdererRecipe(new ItemStack(LPItems.remoteOrderer, 1, i),
					dyes[i - 1],
					new ItemStack(LPItems.remoteOrderer, 1, -1)
			);
			RecipeManager.craftingManager.addShapelessResetRecipe(LPItems.remoteOrderer, i);
		}
		RecipeManager.craftingManager.addShapelessResetRecipe(LPItems.remoteOrderer, 0);
	}
}
