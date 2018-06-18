package logisticspipes.recipes;

import java.util.ArrayList;
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
import logisticspipes.modules.ModuleEnchantmentSinkMK2;
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

public class ModuleChippedCraftingRecipes extends CraftingPartRecipes {
	enum RecipeType{
		LEVEL_1,
		LEVEL_2,
		LEVEL_3,
		UPGRADE_1,
		UPGRADE_2,
		UPGRADE_3,
		LEVEL_4,
		UPGRADE_4,
		UPGRADE_5,
		UPGRADE_6,
		ADVANCED_1,
		ADVANCED_2,
		ADVANCED_3,
		ADVANCED_4
	}

	private void registerModuleRecipe(CraftingParts parts, RecipeType type, Class<? extends LogisticsModule> moduleClass, Class<? extends LogisticsModule> baseModuleClass) {
		Item module = LogisticsPipes.LogisticsModules.get(moduleClass);
		Item baseModule = baseModuleClass == null ? LogisticsPipes.LogisticsBlankModule : LogisticsPipes.LogisticsModules.get(baseModuleClass);
		ItemStack programmerStack = new ItemStack(LogisticsPipes.LogisticsProgrammer);
		programmerStack.setTagCompound(new NBTTagCompound());
		programmerStack.getTagCompound().setString(ItemLogisticsProgrammer.RECIPE_TARGET, module.getRegistryName().toString());
		Ingredient programmer = NBTIngredient.fromStacks(programmerStack);
		RecipeManager.RecipeLayout layout = null;
		switch (type) {
			case LEVEL_1:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"rfr",
						"imi"
				);
				break;
			case LEVEL_2:
				layout = new RecipeManager.RecipeLayout(
						"fpf",
						"rbr",
						"imi"
				);
				break;
			case LEVEL_3:
				layout = new RecipeManager.RecipeLayout(
						"fpf",
						"rar",
						"gmg"
				);
				break;
			case UPGRADE_1:
				layout = new RecipeManager.RecipeLayout(
						"p",
						"f",
						"m"
				);
				break;
			case UPGRADE_2:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"rfr",
						"gmg"
				);
				break;
			case UPGRADE_3:
				layout = new RecipeManager.RecipeLayout(
						"bpb",
						"rar",
						"gmg"
				);
				break;
			case LEVEL_4:
				layout = new RecipeManager.RecipeLayout(
						"fpf",
						"lbl",
						"imi"
				);
				break;
			case UPGRADE_4:
				layout = new RecipeManager.RecipeLayout(
						"fpf",
						"lal",
						"gmg"
				);
				break;
			case UPGRADE_5:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"rbr",
						"imi"
				);
				break;
			case UPGRADE_6:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"rbr",
						"gmg"
				);
				break;
			case ADVANCED_1:
				layout = new RecipeManager.RecipeLayout(
						"fpf",
						"lbl",
						"gmg"
				);
				break;
			case ADVANCED_2:
				layout = new RecipeManager.RecipeLayout(
						"bpb",
						"lal",
						"gmg"
				);
				break;
			case ADVANCED_3:
				layout = new RecipeManager.RecipeLayout(
						"apa",
						"zbz",
						"gmg"
				);
				break;
			case ADVANCED_4:
				layout = new RecipeManager.RecipeLayout(
						"bpb",
						"zaz",
						"gmg"
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
					new RecipeManager.RecipeIndex('i', "ingotIron"),
					new RecipeManager.RecipeIndex('l', "gemLapis"),
					new RecipeManager.RecipeIndex('m', baseModule),
					new RecipeManager.RecipeIndex('p', programmer),
					new RecipeManager.RecipeIndex('r', "dustRedstone"),
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
		registerModuleRecipe(parts, RecipeType.LEVEL_1, ModuleItemSink.class, null);
		registerModuleRecipe(parts, RecipeType.LEVEL_1, ModulePassiveSupplier.class, null);
		registerModuleRecipe(parts, RecipeType.LEVEL_2, ModuleExtractor.class, null);
		registerModuleRecipe(parts, RecipeType.LEVEL_1, ModulePolymorphicItemSink.class, null);
		registerModuleRecipe(parts, RecipeType.LEVEL_3, ModuleQuickSort.class, null);
		registerModuleRecipe(parts, RecipeType.LEVEL_1, ModuleTerminus.class, null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_1, ModuleAdvancedExtractor.class, ModuleExtractor.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, ModuleExtractorMk2.class, ModuleExtractor.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_1, ModuleAdvancedExtractorMK2.class, ModuleExtractorMk2.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, ModuleAdvancedExtractorMK2.class, ModuleAdvancedExtractor.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_3, ModuleExtractorMk3.class, ModuleExtractorMk2.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_1, ModuleAdvancedExtractorMK3.class, ModuleExtractorMk3.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_3, ModuleAdvancedExtractorMK3.class, ModuleAdvancedExtractorMK2.class);
		registerModuleRecipe(parts, RecipeType.LEVEL_4, ModuleProvider.class, null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_4, ModuleProviderMk2.class, ModuleProvider.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, ModuleModBasedItemSink.class, null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, ModuleOreDictItemSink.class, null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_5, ModuleEnchantmentSink.class, ModuleItemSink.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_6, ModuleEnchantmentSinkMK2.class, ModuleEnchantmentSink.class);
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, ModuleCreativeTabBasedItemSink.class, null);
		registerModuleRecipe(parts, RecipeType.ADVANCED_1, ModuleCrafter.class, null);
		registerModuleRecipe(parts, RecipeType.ADVANCED_2, ModuleCrafterMK2.class, ModuleCrafter.class);
		registerModuleRecipe(parts, RecipeType.ADVANCED_3, ModuleCrafterMK3.class, ModuleCrafterMK2.class);
		registerModuleRecipe(parts, RecipeType.ADVANCED_4, ModuleActiveSupplier.class, null);
	}

	@Override
	protected void loadPlainRecipes() {

	}
}
