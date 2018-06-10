package logisticspipes.recipes;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemLogisticsChips;

public class LPChipRecipes implements IRecipeProvider {

	@Override
	public void loadRecipes() {
		boolean copperExsists = !OreDictionary.getOres("ingotCopper", false).isEmpty();

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChips_basic_raw, 2),
				new RecipeManager.RecipeLayoutSmall(
						"sg",
						"gs"
				),
				new RecipeManager.RecipeIndex('s', "sand"),
				new RecipeManager.RecipeIndex('g', copperExsists ? "ingotCopper" : "ingotGold"));

		GameRegistry.addSmelting(new ItemStack(LogisticsPipes.LogisticsChips_basic_raw, 1), new ItemStack(LogisticsPipes.LogisticsChips_basic, 1), 0);

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChips_advanced_raw, 2),
				new RecipeManager.RecipeLayoutSmall(
						"cd",
						"dc"
				),
				new RecipeManager.RecipeIndex('c', new ItemStack(LogisticsPipes.LogisticsChips_basic, 1)),
				new RecipeManager.RecipeIndex('d', "gemDiamond"));

		GameRegistry.addSmelting(new ItemStack(LogisticsPipes.LogisticsChips_advanced_raw, 1), new ItemStack(LogisticsPipes.LogisticsChips_advanced, 1), 0);

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChips_fpga_raw, 16),
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

		GameRegistry.addSmelting(new ItemStack(LogisticsPipes.LogisticsChips_fpga_raw, 1), new ItemStack(LogisticsPipes.LogisticsChips_fpga, 1), 0);
	}
}
