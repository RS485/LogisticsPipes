package logisticspipes.recipes;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemLogisticsChips;

public class LPChipRecipes implements IRecipeProvider {

	@Override
	public void loadRecipes() {
		boolean copperExsists = !OreDictionary.getOres("ingotCopper", false).isEmpty();

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChips, 2, ItemLogisticsChips.ITEM_CHIP_BASIC_RAW), CraftingDependency.Basic,
				new RecipeManager.RecipeLayoutSmall(
						"sg",
						"gs"
				),
				new RecipeManager.RecipeIndex('s', "sand"),
				new RecipeManager.RecipeIndex('g', copperExsists ? "ingotCopper" : "ingotGold"));

		GameRegistry.addSmelting(new ItemStack(LogisticsPipes.LogisticsChips, 1, ItemLogisticsChips.ITEM_CHIP_BASIC_RAW), new ItemStack(LogisticsPipes.LogisticsChips, 1, ItemLogisticsChips.ITEM_CHIP_BASIC), 0);

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChips, 2, ItemLogisticsChips.ITEM_CHIP_ADVANCED_RAW), CraftingDependency.Basic,
				new RecipeManager.RecipeLayoutSmall(
						"cd",
						"dc"
				),
				new RecipeManager.RecipeIndex('c', new ItemStack(LogisticsPipes.LogisticsChips, 1, ItemLogisticsChips.ITEM_CHIP_BASIC)),
				new RecipeManager.RecipeIndex('d', "gemDiamond"));

		GameRegistry.addSmelting(new ItemStack(LogisticsPipes.LogisticsChips, 1, ItemLogisticsChips.ITEM_CHIP_ADVANCED_RAW), new ItemStack(LogisticsPipes.LogisticsChips, 1, ItemLogisticsChips.ITEM_CHIP_ADVANCED), 0);

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChips, 16, ItemLogisticsChips.ITEM_CHIP_FPGA_RAW), CraftingDependency.Basic,
				new RecipeManager.RecipeLayout(
						"sds",
						"grg",
						"sls"
				),
				new RecipeManager.RecipeIndex('s', "sand"),
				new RecipeManager.RecipeIndex('d', "gemDiamond"),
				new RecipeManager.RecipeIndex('g', "ingotGold"),
				new RecipeManager.RecipeIndex('r', "dustRedstone"),
				new RecipeManager.RecipeIndex('l', "gemLapis"));

		GameRegistry.addSmelting(new ItemStack(LogisticsPipes.LogisticsChips, 1, ItemLogisticsChips.ITEM_CHIP_FPGA_RAW), new ItemStack(LogisticsPipes.LogisticsChips, 1, ItemLogisticsChips.ITEM_CHIP_FPGA), 0);
	}
}
