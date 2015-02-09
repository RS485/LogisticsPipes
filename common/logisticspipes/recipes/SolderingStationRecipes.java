package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ICraftingResultHandler;
import logisticspipes.proxy.interfaces.ICraftingParts;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SolderingStationRecipes {
	
	public static class SolderingStationRecipe {
		public final ItemStack[] source;
		public final ItemStack result;
		public final ICraftingResultHandler handler;
		public SolderingStationRecipe(ItemStack[] stacks, ItemStack result, ICraftingResultHandler handler) {
			this.source = stacks;
			this.result = result;
			this.handler = handler;
		}
	}
	
	private static final ArrayList<SolderingStationRecipe> recipes = new ArrayList<SolderingStationRecipe>();
	
	public static void loadRecipe(ICraftingParts parts) {
		recipes.add(new SolderingStationRecipe(new ItemStack[] {
					new ItemStack(LogisticsPipes.PipeParts, 1, 7),
					null,
					new ItemStack(LogisticsPipes.PipeParts, 1, 7),
					new ItemStack(LogisticsPipes.ModuleItem,1,0),
					new ItemStack(Items.ender_pearl,1),
					new ItemStack(LogisticsPipes.ModuleItem,1,0),
					null,
					null,
					null }, 
				new ItemStack(LogisticsPipes.LogisticsItemCard,2,0), new ICraftingResultHandler() {
			@Override
			public void handleCrafting(ItemStack stack) {
				stack.stackTagCompound = new NBTTagCompound();
				stack.stackTagCompound.setString("UUID", UUID.randomUUID().toString());
			}
		}));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[] {
					new ItemStack(LogisticsPipes.LogisticsParts,1,0),
					null,
					new ItemStack(LogisticsPipes.LogisticsParts,1,0),
					new ItemStack(LogisticsPipes.LogisticsParts,1,1),
					new ItemStack(LogisticsPipes.LogisticsParts,1,2),
					new ItemStack(LogisticsPipes.LogisticsParts,1,1),
					null,
					null,
					null }, 
				new ItemStack(LogisticsPipes.LogisticsHUDArmor,1,0), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					new ItemStack(Items.redstone,1,0),
					new ItemStack(LogisticsPipes.PipeParts, 1, 11),
					new ItemStack(Items.redstone,1,0),
					new ItemStack(Items.redstone,1,0),
					new ItemStack(Blocks.glass,1,0),
					new ItemStack(Items.redstone,1,0),
					new ItemStack(Items.redstone,1,0),
					new ItemStack(Items.redstone,1,0),
					new ItemStack(Items.redstone,1,0) }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,1), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					null,
					null,
					null,
					new ItemStack(Items.diamond,1,0),
					new ItemStack(Items.diamond,1,0),
					new ItemStack(Items.diamond,1,0),
					null,
					null,
					new ItemStack(LogisticsPipes.PipeParts, 1, 11) }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,0), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					null,
					new ItemStack(LogisticsPipes.PipeParts, 1, 11),
					null,
					new ItemStack(Items.diamond,1,0),
					null,
					new ItemStack(Items.diamond,1,0),
					null,
					null,
					null }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,2), null));

		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					new ItemStack(Blocks.hopper,1),
					new ItemStack(Blocks.hopper,1),
					new ItemStack(Blocks.hopper,1),
					new ItemStack(Blocks.hopper,1),
					null,
					new ItemStack(Blocks.hopper,1),
					new ItemStack(Blocks.hopper,1),
					new ItemStack(Blocks.hopper,1),
					new ItemStack(Blocks.hopper,1) }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,3), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					null,
					new ItemStack(LogisticsPipes.LogisticsParts,1,3),
					null,
					null,
					new ItemStack(LogisticsPipes.PipeParts, 1, 8),
					null,
					null,
					new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2,1),null }, 
				new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk3,1), null));

		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					new ItemStack(Blocks.nether_brick,1),
					new ItemStack(LogisticsPipes.PipeParts, 1, 9),
					new ItemStack(Blocks.nether_brick,1),
					new ItemStack(LogisticsPipes.PipeParts, 1, 7),
					new ItemStack(LogisticsPipes.LogisticsBasicPipe,1),
					new ItemStack(LogisticsPipes.PipeParts, 1, 15),
					new ItemStack(Blocks.nether_brick,1),
					new ItemStack(LogisticsPipes.PipeParts, 1, 8),
					new ItemStack(Blocks.nether_brick,1) }, 
				new ItemStack(LogisticsPipes.LogisticsFirewallPipe,1), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
				new ItemStack(Blocks.stonebrick,1),
				new ItemStack(Blocks.crafting_table,1),
				new ItemStack(Blocks.stonebrick,1),
				new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2,1),
				new ItemStack(Blocks.hopper,1),
				new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2,1),
				new ItemStack(LogisticsPipes.PipeParts, 1, 3),
				new ItemStack(Blocks.chest,3),
				new ItemStack(LogisticsPipes.PipeParts, 1, 4)}, 
			new ItemStack(LogisticsPipes.logisticsRequestTable,1), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[] {
				new ItemStack(Items.redstone,1),
				new ItemStack(Items.diamond,1),
				new ItemStack(Items.redstone,1),
				null,
				new ItemStack(LogisticsPipes.PipeParts, 1, 3),
				null,
				new ItemStack(Items.redstone,1),
				new ItemStack(Items.diamond,1),
				new ItemStack(Items.redstone,1)},
			new ItemStack(LogisticsPipes.PipeParts, 1, 5), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[] {
				new ItemStack(Items.dye,1,14),
				new ItemStack(Items.diamond,1),
				new ItemStack(Items.dye,1,14),
				null,
				new ItemStack(LogisticsPipes.PipeParts, 1, 3),
				null,
				new ItemStack(Items.dye,1,14),
				new ItemStack(Items.diamond,1),
				new ItemStack(Items.dye,1,14)},
			new ItemStack(LogisticsPipes.PipeParts, 1, 6), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[] {
				null,
				null,
				null,
				new ItemStack(Items.diamond,1),
				new ItemStack(LogisticsPipes.PipeParts, 1, 7),
				new ItemStack(Items.diamond,1),
				null,
				null,
				null},
			new ItemStack(LogisticsPipes.PipeParts, 1, 9), null));
	}
	
	public static List<SolderingStationRecipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}
}
