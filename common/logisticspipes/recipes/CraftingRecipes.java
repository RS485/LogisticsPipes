package logisticspipes.recipes;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.LPItems;
import logisticspipes.items.ItemModule;
import logisticspipes.modules.LogisticsModule;

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
