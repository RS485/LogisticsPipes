package logisticspipes.recipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import logisticspipes.LPItems;
import logisticspipes.items.ItemModule;
import logisticspipes.modules.LogisticsModule;

public class CraftingRecipes implements IRecipeProvider {

	@Override
	public void loadRecipes() {
		// @formatter:off
		String[] dyes = {
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
		// @formatter:on

		registerResetRecipe(dyes);
	}

	private void registerResetRecipe(String[] dyes) {
		for (ResourceLocation moduleResource : LPItems.modules.values()) {
			final Item item = Item.REGISTRY.getObject(moduleResource);
			if (item instanceof ItemModule) {
				LogisticsModule module = ((ItemModule) item).getModuleForItem(new ItemStack(item), null, null, null);
				if (module == null) continue;
				NBTTagCompound tag = new NBTTagCompound();
				module.writeToNBT(tag);
				if (!tag.hasNoTags()) {
					RecipeManager.craftingManager.addShapelessResetRecipe(item, 0);
				}
			}
		}

		for (int i = 1; i < 17; i++) {
			RecipeManager.craftingManager.addOrdererRecipe(new ItemStack(LPItems.remoteOrderer, 1, i), dyes[i - 1], new ItemStack(LPItems.remoteOrderer, 1, -1));
			RecipeManager.craftingManager.addShapelessResetRecipe(LPItems.remoteOrderer, i);
		}
		RecipeManager.craftingManager.addShapelessResetRecipe(LPItems.remoteOrderer, 0);
	}
}
