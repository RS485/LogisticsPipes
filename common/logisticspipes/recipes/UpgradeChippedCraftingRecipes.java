package logisticspipes.recipes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemLogisticsProgrammer;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleAdvancedExtractorMK2;
import logisticspipes.modules.ModuleAdvancedExtractorMK3;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleCrafterMK2;
import logisticspipes.modules.ModuleCrafterMK3;
import logisticspipes.modules.ModuleCreativeTabBasedItemSink;
import logisticspipes.modules.ModuleEnchantmentSink;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleExtractorMk2;
import logisticspipes.modules.ModuleExtractorMk3;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModulePolymorphicItemSink;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleProviderMk2;
import logisticspipes.modules.ModuleQuickSort;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.modules.abstractmodules.LogisticsModule;
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

	private void registerUpgradeRecipe(CraftingParts parts, RecipeType type, Class<? extends IPipeUpgrade> upgradeClass) {
		Item module = LogisticsPipes.LogisticsUpgrades.get(upgradeClass);

		ItemStack programmerStack = new ItemStack(LogisticsPipes.LogisticsProgrammer);
		programmerStack.setTagCompound(new NBTTagCompound());
		programmerStack.getTagCompound().setString(ItemLogisticsProgrammer.RECIPE_TARGET, module.getRegistryName().toString());
		Ingredient programmer = NBTIngredient.fromStacks(programmerStack);
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
			RecipeManager.craftingManager.addRecipe(new ItemStack(module), indexToUse.toArray());
		}
	}

	@Override
	protected void loadRecipes(CraftingParts parts) {
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, SneakyUpgradeConfig.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, SpeedUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, CombinedSneakyUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, ConnectionUpgradeConfig.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_2, AdvancedSatelliteUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, FluidCraftingUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, CraftingByproductUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, PatternUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, FuzzyUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, PowerTransportationUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, CraftingMonitoringUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_1, OpaqueUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_2, CraftingCleanupUpgrade.class);
		registerUpgradeRecipe(parts, RecipeType.LEVEL_3, UpgradeModuleUpgrade.class);
	}

	@Override
	protected void loadPlainRecipes() {

	}
}
