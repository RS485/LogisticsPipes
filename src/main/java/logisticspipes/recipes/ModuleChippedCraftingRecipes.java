package logisticspipes.recipes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import logisticspipes.LPItems;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleCreativeTabBasedItemSink;
import logisticspipes.modules.ModuleEnchantmentSink;
import logisticspipes.modules.ModuleEnchantmentSinkMK2;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModulePolymorphicItemSink;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleTerminus;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;
import network.rs485.logisticspipes.module.AsyncExtractorModule;
import network.rs485.logisticspipes.module.AsyncQuicksortModule;

public class ModuleChippedCraftingRecipes extends CraftingPartRecipes {

	enum RecipeType {
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

	private void registerModuleRecipe(CraftingParts parts, RecipeType type, ResourceLocation recipeCategory, @Nonnull String moduleName, @Nullable String baseModuleName) {
		final ResourceLocation moduleResource = LPItems.modules.get(moduleName);
		Item module = Item.REGISTRY.getObject(moduleResource);
		if (module == null) return;
		Item baseModule;
		if (baseModuleName == null) {
			baseModule = LPItems.blankModule;
		} else {
			baseModule = Item.REGISTRY.getObject(LPItems.modules.get(baseModuleName));
		}
		if (baseModule == null) return;

		Ingredient programmer = programmerIngredient(moduleResource.toString());
		final Set<ResourceLocation> compilerPrograms = LogisticsProgramCompilerTileEntity.programByCategory.putIfAbsent(recipeCategory, new HashSet<>());
		Objects.requireNonNull(compilerPrograms).add(moduleResource);

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
		if (layout != null) {
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
		registerModuleRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, ModuleItemSink.getName(), null);
		registerModuleRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, ModulePassiveSupplier.getName(), null);
		registerModuleRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, AsyncExtractorModule.getName(), null);
		registerModuleRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, ModulePolymorphicItemSink.getName(), null);
		registerModuleRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, AsyncQuicksortModule.getName(), null);
		registerModuleRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, ModuleTerminus.getName(), null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, AsyncAdvancedExtractor.getName(), AsyncExtractorModule.getName());
		registerModuleRecipe(parts, RecipeType.LEVEL_4, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, ModuleProvider.getName(), null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS_2, ModuleModBasedItemSink.getName(), null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS_2, ModuleOreDictItemSink.getName(), null);
		registerModuleRecipe(parts, RecipeType.UPGRADE_5, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS_2, ModuleEnchantmentSink.getName(), ModuleItemSink.getName());
		registerModuleRecipe(parts, RecipeType.UPGRADE_6, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS_2, ModuleEnchantmentSinkMK2.getName(), ModuleEnchantmentSink.getName());
		registerModuleRecipe(parts, RecipeType.UPGRADE_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS_2, ModuleCreativeTabBasedItemSink.getName(), null);
		registerModuleRecipe(parts, RecipeType.ADVANCED_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS_2, ModuleCrafter.getName(), null);
		registerModuleRecipe(parts, RecipeType.ADVANCED_4, LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS_2, ModuleActiveSupplier.getName(), null);
	}

}
