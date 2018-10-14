package logisticspipes.recipes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import logisticspipes.LPItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.items.ItemLogisticsProgrammer;
import logisticspipes.pipes.upgrades.AdvancedSatelliteUpgrade;
import logisticspipes.pipes.upgrades.CombinedSneakyUpgrade;
import logisticspipes.pipes.upgrades.ConnectionUpgradeConfig;
import logisticspipes.pipes.upgrades.CraftingByproductUpgrade;
import logisticspipes.pipes.upgrades.CraftingCleanupUpgrade;
import logisticspipes.pipes.upgrades.CraftingMonitoringUpgrade;
import logisticspipes.pipes.upgrades.FluidCraftingUpgrade;
import logisticspipes.pipes.upgrades.FuzzyUpgrade;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.OpaqueUpgrade;
import logisticspipes.pipes.upgrades.PatternUpgrade;
import logisticspipes.pipes.upgrades.PowerTransportationUpgrade;
import logisticspipes.pipes.upgrades.SneakyUpgradeConfig;
import logisticspipes.pipes.upgrades.SpeedUpgrade;
import logisticspipes.pipes.upgrades.UpgradeModuleUpgrade;

public class UpgradeChippedCraftingRecipes extends CraftingPartRecipes {
	enum RecipeType{
		LEVEL_1,
		LEVEL_2,
		LEVEL_3,
	}

	private void registerUpgradeRecipe(CraftingParts parts, RecipeType type, ResourceLocation recipeCategory, Class<? extends IPipeUpgrade> upgradeClass) {
		Item upgrade = LPItems.upgrades.get(upgradeClass);

		ItemStack programmerStack = new ItemStack(LPItems.logisticsProgrammer);
		programmerStack.setTagCompound(new NBTTagCompound());
		programmerStack.getTagCompound().setString(ItemLogisticsProgrammer.RECIPE_TARGET, upgrade.getRegistryName().toString());
		Ingredient programmer = NBTIngredient.fromStacks(programmerStack);

		if(!LogisticsProgramCompilerTileEntity.programByCategory.containsKey(recipeCategory)) {
			LogisticsProgramCompilerTileEntity.programByCategory.put(recipeCategory, new HashSet<>());
		}
		LogisticsProgramCompilerTileEntity.programByCategory.get(recipeCategory).add(upgrade.getRegistryName());

		RecipeManager.RecipeLayout layout = null;
		switch (type) {
			case LEVEL_1:
				layout = new RecipeManager.RecipeLayout(
						"rpr",
						"ibi",
						"qnq"
				);
				break;
			case LEVEL_2:
				layout = new RecipeManager.RecipeLayout(
						"rpr",
						"iai",
						"qnq"
				);
				break;
			case LEVEL_3:
				layout = new RecipeManager.RecipeLayout(
						"rpr",
						"gag",
						"qnq"
				);
				break;
		}
		if(layout != null) {
			final RecipeManager.RecipeLayout fLayout = layout;
			List<RecipeManager.RecipeIndex> recipeIndexes = Arrays.asList(
					new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
					new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
					new RecipeManager.RecipeIndex('f', parts.getChipFpga()),
					new RecipeManager.RecipeIndex('g', "ingotGold"),
					new RecipeManager.RecipeIndex('n', "nuggetGold"),
					new RecipeManager.RecipeIndex('i', "ingotIron"),
					new RecipeManager.RecipeIndex('l', "gemLapis"),
					new RecipeManager.RecipeIndex('p', programmer),
					new RecipeManager.RecipeIndex('r', "dustRedstone"),
					new RecipeManager.RecipeIndex('q', "paper"),
					new RecipeManager.RecipeIndex('z', Items.BLAZE_POWDER));
			LinkedList<Object> indexToUse = recipeIndexes.stream()
					.filter(recipeIndex -> !(fLayout.getLine1() + fLayout.getLine2() + fLayout.getLine3()).replace(recipeIndex.getIndex(), ' ')
							.equals((fLayout.getLine1() + fLayout.getLine2() + fLayout.getLine3()))).collect(Collectors.toCollection(LinkedList::new));
			indexToUse.addFirst(layout);
			RecipeManager.craftingManager.addRecipe(new ItemStack(upgrade), indexToUse.toArray());
		}
	}

	@Override
	protected void loadRecipes(CraftingParts parts) {
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, SneakyUpgradeConfig.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, SpeedUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, CombinedSneakyUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, ConnectionUpgradeConfig.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, AdvancedSatelliteUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, FluidCraftingUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, CraftingByproductUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, PatternUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, FuzzyUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, PowerTransportationUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, CraftingMonitoringUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, OpaqueUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, CraftingCleanupUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_3, UpgradeModuleUpgrade.class);
	}

	@Override
	protected void loadPlainRecipes() {

	}
}
