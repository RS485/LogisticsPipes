package logisticspipes.recipes;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

import logisticspipes.LPItems;

public class LPChipRecipes implements IRecipeProvider {

	@Override
	public void loadRecipes() {
		GameRegistry.addSmelting(new ItemStack(LPItems.chipBasicRaw, 1), new ItemStack(LPItems.chipBasic, 1), 0);
		GameRegistry.addSmelting(new ItemStack(LPItems.chipAdvancedRaw, 1), new ItemStack(LPItems.chipAdvanced, 1), 0);
		GameRegistry.addSmelting(new ItemStack(LPItems.chipFPGARaw, 1), new ItemStack(LPItems.chipFPGA, 1), 0);
	}

}
