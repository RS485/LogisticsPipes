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

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.items.ItemLogisticsProgrammer;
import logisticspipes.pipes.upgrades.IPipeUpgrade;

public class PipeChippedCraftingRecipes extends CraftingPartRecipes {
	enum RecipeType{
		LEVEL_1,
		LEVEL_2,
		LEVEL_3,
		ENDER_1,
		ENDER_2,
	}

	private void registerPipeRecipeCategory(ResourceLocation recipeCategory, Item targetPipe) {
		if(!LogisticsProgramCompilerTileEntity.programByCategory.containsKey(recipeCategory)) {
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
		ItemStack programmerStack = new ItemStack(LogisticsPipes.LogisticsProgrammer);
		programmerStack.setTagCompound(new NBTTagCompound());
		programmerStack.getTagCompound().setString(ItemLogisticsProgrammer.RECIPE_TARGET, targetPipe.getRegistryName().toString());
		return NBTIngredient.fromStacks(programmerStack);
	}

	@Override
	protected void loadRecipes(CraftingParts parts) {
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe),
				new RecipeManager.RecipeLayoutSmall(
						"f",
						"p"
				),
				new RecipeManager.RecipeIndex('f', parts.getChipFpga()),
				new RecipeManager.RecipeIndex('p', new ItemStack(LogisticsPipes.BasicTransportPipe)));

		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LogisticsPipes.LogisticsRequestPipeMk1, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LogisticsPipes.LogisticsProviderPipeMk1, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LogisticsPipes.LogisticsCraftingPipeMk1, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LogisticsPipes.LogisticsSatellitePipe, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.BASIC, LogisticsPipes.LogisticsSupplierPipe, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LogisticsPipes.LogisticsCraftingPipeMk2, LogisticsPipes.LogisticsCraftingPipeMk1);
		registerPipeRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LogisticsPipes.LogisticsRequestPipeMk2, LogisticsPipes.LogisticsRequestPipeMk1);
		registerPipeRecipe(parts, RecipeType.ENDER_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LogisticsPipes.LogisticsRemoteOrdererPipe, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_3, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LogisticsPipes.LogisticsProviderPipeMk2, LogisticsPipes.LogisticsProviderPipeMk1);
		registerPipeRecipe(parts, RecipeType.ENDER_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_3, LogisticsPipes.LogisticsInvSysConPipe, LogisticsPipes.LogisticsBasicPipe);

		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LogisticsPipes.LogisticsEntrancePipe, LogisticsPipes.LogisticsProviderPipeMk1);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_2, LogisticsPipes.LogisticsDestinationPipe, LogisticsPipes.LogisticsProviderPipeMk1);
		registerPipeRecipe(parts, RecipeType.ENDER_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.TIER_3, LogisticsPipes.LogisticsFirewallPipe, LogisticsPipes.LogisticsBasicPipe);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LogisticsPipes.LogisticsChassisPipeMk1);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk1),
				new RecipeManager.RecipeLayout(
						" p ",
						" b ",
						"fsf"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LogisticsPipes.LogisticsChassisPipeMk1)),
				new RecipeManager.RecipeIndex('s', LogisticsPipes.LogisticsBasicPipe),
				new RecipeManager.RecipeIndex('f', parts.getChipFpga())
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LogisticsPipes.LogisticsChassisPipeMk2);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk2),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"ili"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LogisticsPipes.LogisticsChassisPipeMk2)),
				new RecipeManager.RecipeIndex('s', LogisticsPipes.LogisticsChassisPipeMk1),
				new RecipeManager.RecipeIndex('l', "gemLapis"),
				new RecipeManager.RecipeIndex('i', "ingotIron")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LogisticsPipes.LogisticsChassisPipeMk3);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk3),
				new RecipeManager.RecipeLayout(
						" p ",
						"gsg",
						"iai"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LogisticsPipes.LogisticsChassisPipeMk3)),
				new RecipeManager.RecipeIndex('s', LogisticsPipes.LogisticsChassisPipeMk2),
				new RecipeManager.RecipeIndex('g', "dustGlowstone"),
				new RecipeManager.RecipeIndex('i', "ingotIron")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LogisticsPipes.LogisticsChassisPipeMk4);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk4),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"gag"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LogisticsPipes.LogisticsChassisPipeMk4)),
				new RecipeManager.RecipeIndex('s', LogisticsPipes.LogisticsChassisPipeMk3),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('g', "ingotGold")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.CHASSIS, LogisticsPipes.LogisticsChassisPipeMk5);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk5),
				new RecipeManager.RecipeLayout(
						" p ",
						"asa",
						"dnd"
				),
				new RecipeManager.RecipeIndex('a', parts.getChipAdvanced()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LogisticsPipes.LogisticsChassisPipeMk5)),
				new RecipeManager.RecipeIndex('s', LogisticsPipes.LogisticsChassisPipeMk4),
				new RecipeManager.RecipeIndex('d', "gemDiamond"),
				new RecipeManager.RecipeIndex('n', "gemQuartz")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidSupplierPipeMk1);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSupplierPipeMk1),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"iwi"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LogisticsPipes.LogisticsFluidSupplierPipeMk1)),
				new RecipeManager.RecipeIndex('s', LogisticsPipes.LogisticsBasicPipe),
				new RecipeManager.RecipeIndex('w', Items.BUCKET),
				new RecipeManager.RecipeIndex('i', "ingotIron")
		);

		registerPipeRecipeCategory(LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidBasicPipe);
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidBasicPipe),
				new RecipeManager.RecipeLayout(
						" p ",
						"bsb",
						"gwg"
				),
				new RecipeManager.RecipeIndex('b', parts.getChipBasic()),
				new RecipeManager.RecipeIndex('p', getIngredientForProgrammer(LogisticsPipes.LogisticsFluidBasicPipe)),
				new RecipeManager.RecipeIndex('s', LogisticsPipes.LogisticsBasicPipe),
				new RecipeManager.RecipeIndex('w', Items.BUCKET),
				new RecipeManager.RecipeIndex('g', "ingotGold")
		);

		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidRequestPipe, LogisticsPipes.LogisticsFluidBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidProviderPipe, LogisticsPipes.LogisticsFluidBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidSupplierPipeMk2, LogisticsPipes.LogisticsFluidSupplierPipeMk1);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidSatellitePipe, LogisticsPipes.LogisticsFluidBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidInsertionPipe, LogisticsPipes.LogisticsFluidBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsProgramCompilerTileEntity.ProgrammCategories.FLUID, LogisticsPipes.LogisticsFluidExtractorPipe, LogisticsPipes.LogisticsFluidBasicPipe);

	}

	@Override
	protected void loadPlainRecipes() {
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.HSTubeLine, 3),
				new RecipeManager.RecipeLayoutSmaller(
						"ppp"
				),
				new RecipeManager.RecipeIndex('p', LogisticsPipes.BasicTransportPipe));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.HSTubeSpeedup),
				new RecipeManager.RecipeLayoutSmaller(
						"ppp"
				),
				new RecipeManager.RecipeIndex('p', LogisticsPipes.HSTubeLine));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.HSTubeCurve),
				new RecipeManager.RecipeLayout(
						"ppp",
						"p  ",
						"p  "
				),
				new RecipeManager.RecipeIndex('p', LogisticsPipes.HSTubeLine));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.HSTubeSCurve),
				new RecipeManager.RecipeLayoutSmall(
						"pp ",
						" pp"
				),
				new RecipeManager.RecipeIndex('p', LogisticsPipes.HSTubeLine));

		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.HSTubeGain),
				new RecipeManager.RecipeLayout(
						"p ",
						"pp",
						" p"
				),
				new RecipeManager.RecipeIndex('p', LogisticsPipes.HSTubeLine));

	}
}
