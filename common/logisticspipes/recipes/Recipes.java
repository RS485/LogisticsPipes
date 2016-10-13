package logisticspipes.recipes;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.modules.abstractmodules.LogisticsModule;

//@formatter:off
//CHECKSTYLE:OFF

public class Recipes implements IRecipeProvider {
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



		for(int i=0; i<1000;i++) {
			LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(new ItemStack(LogisticsPipes.ModuleItem, 1, i), null, null, null);
			if(module != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				boolean force = false;
				try {
					module.writeToNBT(nbt);
				} catch(Exception e) {
					force = true;
				}
				if(!nbt.equals(new NBTTagCompound()) || force) {
					RecipeManager.craftingManager.addShapelessResetRecipe(LogisticsPipes.ModuleItem, i);
				}
			}
		}

		for(int i=1;i<17;i++) {
			RecipeManager.craftingManager.addOrdererRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, i),
					dyes[i - 1],
					new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, -1)
			);
			RecipeManager.craftingManager.addShapelessResetRecipe(LogisticsPipes.LogisticsRemoteOrderer, i);
		}
		RecipeManager.craftingManager.addShapelessResetRecipe(LogisticsPipes.LogisticsRemoteOrderer, 0);

		ItemStack logisticsBlockFrame = new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_BLOCK_FRAME);

		RecipeManager.craftingManager.addRecipe(logisticsBlockFrame, CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						"iri",
						"   ",
						"w w"),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('w', "plankWood"));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.SOLDERING_STATION), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" c ",
						"ifi",
						"ibi"
				),
				new RecipeManager.RecipeIndex('c', Blocks.crafting_table),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('b', Items.blaze_powder));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						" c ",
						"wfw",
						" p "
				),
				new RecipeManager.RecipeIndex('c', Blocks.crafting_table),
				new RecipeManager.RecipeIndex('w', "plankWood"),
				new RecipeManager.RecipeIndex('f', logisticsBlockFrame),
				new RecipeManager.RecipeIndex('p', Blocks.piston));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.BasicTransportPipe, 8), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						"iri",
						"g g",
						"iri"
				),
				new RecipeManager.RecipeIndex('i', "ingotIron"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('g', "blockGlass"));
	}
}
