package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.ICraftingResultHandler;

//@formatter:off
//CHECKSTYLE:OFF

public class SolderingStationRecipes {

	public static class SolderingStationRecipe {
		public final ItemStack[] source;
		public final ItemStack result;
		public final ICraftingResultHandler handler;
		public SolderingStationRecipe(ItemStack[] stacks, ItemStack result, ICraftingResultHandler handler) {
			source = stacks;
			this.result = result;
			this.handler = handler;
		}
	}

	private static final ArrayList<SolderingStationRecipe> recipes = new ArrayList<>();

	public static void loadRecipe(CraftingParts parts) {
		/*
		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[] {
				parts.getChipTear1(),
				ItemStack.EMPTY,
				parts.getChipTear1(),
				new ItemStack(LogisticsPipes.ModuleItem,1,0),
				new ItemStack(Items.ender_pearl,1),
				new ItemStack(LogisticsPipes.ModuleItem,1,0),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY },
				new ItemStack(LogisticsPipes.LogisticsItemCard,2,0), stack-> {
					stack.stackTagCompound = new NBTTagCompound();
					stack.stackTagCompound.setString("UUID", UUID.randomUUID().toString());
				}));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[] {
				new ItemStack(LogisticsPipes.LogisticsParts,1,0),
				ItemStack.EMPTY,
				new ItemStack(LogisticsPipes.LogisticsParts,1,0),
				new ItemStack(LogisticsPipes.LogisticsParts,1,1),
				new ItemStack(LogisticsPipes.LogisticsParts,1,2),
				new ItemStack(LogisticsPipes.LogisticsParts,1,1),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY },
				new ItemStack(LogisticsPipes.LogisticsHUDArmor,1,0), null));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[]{
				new ItemStack(Items.redstone,1,0),
				parts.getChipTear3(),
				new ItemStack(Items.redstone,1,0),
				new ItemStack(Items.redstone,1,0),
				new ItemStack(Blocks.glass,1,0),
				new ItemStack(Items.redstone,1,0),
				new ItemStack(Items.redstone,1,0),
				new ItemStack(Items.redstone,1,0),
				new ItemStack(Items.redstone,1,0) },
				new ItemStack(LogisticsPipes.LogisticsParts,1,1), null));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[]{
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.diamond,1,0),
				new ItemStack(Items.diamond,1,0),
				new ItemStack(Items.diamond,1,0),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				parts.getChipTear3() },
				new ItemStack(LogisticsPipes.LogisticsParts,1,0), null));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[]{
				ItemStack.EMPTY,
				parts.getChipTear3(),
				ItemStack.EMPTY,
				new ItemStack(Items.diamond,1,0),
				ItemStack.EMPTY,
				new ItemStack(Items.diamond,1,0),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY },
				new ItemStack(LogisticsPipes.LogisticsParts,1,2), null));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[]{
				new ItemStack(Blocks.hopper,1),
				new ItemStack(Blocks.hopper,1),
				new ItemStack(Blocks.hopper,1),
				new ItemStack(Blocks.hopper,1),
				ItemStack.EMPTY,
				new ItemStack(Blocks.hopper,1),
				new ItemStack(Blocks.hopper,1),
				new ItemStack(Blocks.hopper,1),
				new ItemStack(Blocks.hopper,1) },
				new ItemStack(LogisticsPipes.LogisticsParts,1,3), null));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[]{
				ItemStack.EMPTY,
				new ItemStack(LogisticsPipes.LogisticsParts,1,3),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				parts.getChipTear3(),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2,1),null },
				new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk3,1), null));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[]{
				new ItemStack(Blocks.nether_brick,1),
				parts.getChipTear3(),
				new ItemStack(Blocks.nether_brick,1),
				ItemStack.EMPTY,
				new ItemStack(LogisticsPipes.LogisticsBasicPipe,1),
				ItemStack.EMPTY,
				new ItemStack(Blocks.nether_brick,1),
				ItemStack.EMPTY,
				new ItemStack(Blocks.nether_brick,1) },
				new ItemStack(LogisticsPipes.LogisticsFirewallPipe,1), null));

		SolderingStationRecipes.recipes.add(new SolderingStationRecipe(new ItemStack[]{
				new ItemStack(Blocks.stonebrick,1),
				new ItemStack(Blocks.crafting_table,1),
				new ItemStack(Blocks.stonebrick,1),
				new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2,1),
				new ItemStack(Blocks.hopper,1),
				new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2,1),
				parts.getChipTear3(),
				new ItemStack(Blocks.chest,3),
				parts.getChipTear3()},
				new ItemStack(LogisticsPipes.logisticsRequestTable,1), null));
				*/
	}

	public static List<SolderingStationRecipe> getRecipes() {
		return Collections.unmodifiableList(SolderingStationRecipes.recipes);
	}
}
