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
import logisticspipes.pipes.upgrades.IPipeUpgrade;

public class PipeChippedCraftingRecipes extends CraftingPartRecipes {
	enum RecipeType{
		LEVEL_1,
		LEVEL_2,
		LEVEL_3,
		ENDER_1,
		ENDER_2,
	}

	private void registerPipeRecipe(CraftingParts parts, RecipeType type, Item targetPipe, Item basePipe) {
		ItemStack programmerStack = new ItemStack(LogisticsPipes.LogisticsProgrammer);
		programmerStack.setTagCompound(new NBTTagCompound());
		programmerStack.getTagCompound().setString(ItemLogisticsProgrammer.RECIPE_TARGET, targetPipe.getRegistryName().toString());
		Ingredient programmer = NBTIngredient.fromStacks(programmerStack);
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

	@Override
	protected void loadRecipes(CraftingParts parts) {
		RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe),
				new RecipeManager.RecipeLayoutSmall(
						"f",
						"p"
				),
				new RecipeManager.RecipeIndex('f', parts.getChipFpga()),
				new RecipeManager.RecipeIndex('p', new ItemStack(LogisticsPipes.BasicTransportPipe)));

		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsPipes.LogisticsRequestPipeMk1, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsPipes.LogisticsProviderPipeMk1, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsPipes.LogisticsCraftingPipeMk1, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsPipes.LogisticsSatellitePipe, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsPipes.LogisticsSupplierPipe, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_2, LogisticsPipes.LogisticsCraftingPipeMk2, LogisticsPipes.LogisticsCraftingPipeMk1);
		registerPipeRecipe(parts, RecipeType.LEVEL_3, LogisticsPipes.LogisticsRequestPipeMk2, LogisticsPipes.LogisticsRequestPipeMk1);
		registerPipeRecipe(parts, RecipeType.ENDER_1, LogisticsPipes.LogisticsRemoteOrdererPipe, LogisticsPipes.LogisticsBasicPipe);
		registerPipeRecipe(parts, RecipeType.LEVEL_3, LogisticsPipes.LogisticsProviderPipeMk2, LogisticsPipes.LogisticsProviderPipeMk1);
		registerPipeRecipe(parts, RecipeType.ENDER_1, LogisticsPipes.LogisticsInvSysConPipe, LogisticsPipes.LogisticsBasicPipe);

		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsPipes.LogisticsEntrancePipe, LogisticsPipes.LogisticsProviderPipeMk1);
		registerPipeRecipe(parts, RecipeType.LEVEL_1, LogisticsPipes.LogisticsDestinationPipe, LogisticsPipes.LogisticsProviderPipeMk1);
		registerPipeRecipe(parts, RecipeType.ENDER_2, LogisticsPipes.LogisticsFirewallPipe, LogisticsPipes.LogisticsBasicPipe);
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
