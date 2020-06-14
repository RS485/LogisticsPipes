package logisticspipes.recipes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import logisticspipes.LPItems;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.items.ItemLogisticsProgrammer;

public class PipeChippedCraftingRecipes extends CraftingPartRecipes {

	enum RecipeType {
		LEVEL_1,
		LEVEL_2,
		LEVEL_3,
		ENDER_1,
		ENDER_2,
	}

	private void registerPipeRecipeCategory(ResourceLocation recipeCategory, Item targetPipe) {
		if (!LogisticsProgramCompilerTileEntity.programByCategory.containsKey(recipeCategory)) {
			LogisticsProgramCompilerTileEntity.programByCategory.put(recipeCategory, new HashSet<>());
		}
		LogisticsProgramCompilerTileEntity.programByCategory.get(recipeCategory).add(targetPipe.getRegistryName());
	}

	private void registerPipeRecipe(CraftingParts parts, RecipeType type, ResourceLocation recipeCategory, Item targetPipe, Item basePipe) {
		Ingredient programmer = getIngredientForProgrammer(targetPipe);

		registerPipeRecipeCategory(recipeCategory, targetPipe);

		RecipeManager.RecipeLayout layout = null;
		switch (type) {
			case LEVEL_1:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"rfr",
						" s "
				);
				break;
			case LEVEL_2:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"rbr",
						"isi"
				);
				break;
			case LEVEL_3:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"rar",
						"gsg"
				);
				break;
			case ENDER_1:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"ebr",
						"isi"
				);
				break;
			case ENDER_2:
				layout = new RecipeManager.RecipeLayout(
						" p ",
						"ear",
						"isi"
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
					new RecipeManager.RecipeIndex('n', "nuggetGold"),
					new RecipeManager.RecipeIndex('i', "ingotIron"),
					new RecipeManager.RecipeIndex('l', "gemLapis"),
					new RecipeManager.RecipeIndex('p', programmer),
					new RecipeManager.RecipeIndex('r', "dustRedstone"),
					new RecipeManager.RecipeIndex('s', basePipe),
					new RecipeManager.RecipeIndex('z', Items.BLAZE_POWDER),
					new RecipeManager.RecipeIndex('e', Items.ENDER_PEARL));
			LinkedList<Object> indexToUse = recipeIndexes.stream()
					.filter(recipeIndex -> !(fLayout.getLine1() + fLayout.getLine2() + fLayout.getLine3()).replace(recipeIndex.getIndex(), ' ')
							.equals((fLayout.getLine1() + fLayout.getLine2() + fLayout.getLine3()))).collect(Collectors.toCollection(LinkedList::new));
			indexToUse.addFirst(layout);
			RecipeManager.craftingManager.addRecipe(new ItemStack(targetPipe), indexToUse.toArray());
		}
	}

	private Ingredient getIngredientForProgrammer(Item targetPipe) {
		ItemStack programmerStack = new ItemStack(LPItems.logisticsProgrammer);
		programmerStack.setTagCompound(new NBTTagCompound());
		programmerStack.getTagCompound().setString(ItemLogisticsProgrammer.RECIPE_TARGET, targetPipe.getRegistryName().toString());
		return NBTIngredient.fromStacks(programmerStack);
	}

	@Override
	protected void loadRecipes(CraftingParts parts) {
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LPItems.pipeRequest, LPItems.pipeBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LPItems.pipeProvider, LPItems.pipeBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LPItems.pipeCrafting, LPItems.pipeBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LPItems.pipeSatellite, LPItems.pipeBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LPItems.pipeSupplier, LPItems.pipeBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LPItems.pipeRequestMk2, LPItems.pipeRequest);
		registerPipeRecipe(parts, RecipeType.ENDER_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LPItems.pipeRemoteOrderer, LPItems.pipeBasic);
		registerPipeRecipe(parts, RecipeType.ENDER_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_3, LPItems.pipeInvSystemConnector, LPItems.pipeBasic);

		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LPItems.pipeSystemEntrance, LPItems.pipeProvider);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LPItems.pipeSystemDestination, LPItems.pipeProvider);
		registerPipeRecipe(parts, RecipeType.ENDER_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_3, LPItems.pipeFirewall, LPItems.pipeBasic);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LPItems.pipeChassisMk1);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeChassisMk1),
				new RecipeManager.RecipeLayout(
						" p ",
						" b ",
						"fsf"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LPItems.pipeChassisMk1)),
				new RecipeManager.RecipeIndex('s', LPItems.pipeBasic),
				new RecipeManager.RecipeIndex('f', parts.getChipFpga())
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LPItems.pipeChassisMk2);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeChassisMk2),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"ili"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LPItems.pipeChassisMk2)),
				new RecipeManager.RecipeIndex('s', LPItems.pipeChassisMk1),
				new RecipeManager.RecipeIndex('l', "gemLapis"),
				new RecipeManager.RecipeIndex('i', "ingotIron")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LPItems.pipeChassisMk3);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeChassisMk3),
				new RecipeManager.RecipeLayout(
						" p ",
						"gsg",
						"iai"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LPItems.pipeChassisMk3)),
				new RecipeManager.RecipeIndex('s', LPItems.pipeChassisMk2),
				new RecipeManager.RecipeIndex('g', "dustGlowstone"),
				new RecipeManager.RecipeIndex('i', "ingotIron")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LPItems.pipeChassisMk4);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeChassisMk4),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"gag"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LPItems.pipeChassisMk4)),
				new RecipeManager.RecipeIndex('s', LPItems.pipeChassisMk3),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('g', "ingotGold")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LPItems.pipeChassisMk5);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeChassisMk5),
				new RecipeManager.RecipeLayout(
						" p ",
						"asa",
						"dnd"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LPItems.pipeChassisMk5)),
				new RecipeManager.RecipeIndex('s', LPItems.pipeChassisMk4),
				new RecipeManager.RecipeIndex('d', "gemDiamond"),
				new RecipeManager.RecipeIndex('n', "gemQuartz")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidSupplier);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeFluidSupplier),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"iwi"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LPItems.pipeFluidSupplier)),
				new RecipeManager.RecipeIndex('s', LPItems.pipeBasic),
				new RecipeManager.RecipeIndex('w', Items.BUCKET),
				new RecipeManager.RecipeIndex('i', "ingotIron")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidBasic);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LPItems.pipeFluidBasic),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"gwg"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LPItems.pipeFluidBasic)),
				new RecipeManager.RecipeIndex('s', LPItems.pipeBasic),
				new RecipeManager.RecipeIndex('w', Items.BUCKET),
				new RecipeManager.RecipeIndex('g', "ingotGold")
		);

		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidRequest, LPItems.pipeFluidBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidProvider, LPItems.pipeFluidBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidSupplierMk2, LPItems.pipeFluidSupplier);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidSatellite, LPItems.pipeFluidBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidInsertion, LPItems.pipeFluidBasic);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LPItems.pipeFluidExtractor, LPItems.pipeFluidBasic);

	}

}
