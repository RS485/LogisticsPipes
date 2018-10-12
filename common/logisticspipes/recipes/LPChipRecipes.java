package logisticspipes.recipes;

import logisticspipes.LPItems;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class LPChipRecipes implements IRecipeProvider {

	@Override
	public void loadRecipes() {
		boolean copperExsists = !OreDictionary.getOres("ingotCopper", false).isEmpty();

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.chipBasicRaw, 2),
				new RecipeManager.RecipeLayoutSmall(
						"sg",
						"gs"
				),
				new RecipeManager.RecipeIndex('s', "sand"),
				new RecipeManager.RecipeIndex('g', copperExsists ? "ingotCopper" : "ingotGold"));

		GameRegistry.addSmelting(new ItemStack(LPItems.chipBasicRaw, 1), new ItemStack(LPItems.chipBasic, 1), 0);

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.chipAdvancedRaw, 2),
				new RecipeManager.RecipeLayoutSmall(
						"cd",
						"dc"
				),
				new RecipeManager.RecipeIndex('c', new ItemStack(LPItems.chipBasicRaw, 1)),
				new RecipeManager.RecipeIndex('d', "gemDiamond"));

		GameRegistry.addSmelting(new ItemStack(LPItems.chipAdvancedRaw, 1), new ItemStack(LPItems.chipAdvanced, 1), 0);

		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.chipFPGARaw, 16),
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

		GameRegistry.addSmelting(new ItemStack(LPItems.chipFPGARaw, 1), new ItemStack(LPItems.chipFPGA, 1), 0);
	}
}
