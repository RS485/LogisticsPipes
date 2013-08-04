package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ICraftingResultHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;

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
	
	public static void loadRecipe() {
		recipes.add(new SolderingStationRecipe(new ItemStack[] {
					new ItemStack(BuildCraftSilicon.redstoneChipset,1,1),
					null,
					new ItemStack(BuildCraftSilicon.redstoneChipset,1,1),
					new ItemStack(LogisticsPipes.ModuleItem,1,0),
					new ItemStack(Item.enderPearl,1),
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
					new ItemStack(Item.redstone,1,0),
					new ItemStack(BuildCraftSilicon.redstoneChipset,1,3),
					new ItemStack(Item.redstone,1,0),
					new ItemStack(Item.redstone,1,0),
					new ItemStack(Block.glass,1,0),
					new ItemStack(Item.redstone,1,0),
					new ItemStack(Item.redstone,1,0),
					new ItemStack(Item.redstone,1,0),
					new ItemStack(Item.redstone,1,0) }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,1), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					null,
					null,
					null,
					new ItemStack(Item.diamond,1,0),
					new ItemStack(Item.diamond,1,0),
					new ItemStack(Item.diamond,1,0),
					null,
					null,
					new ItemStack(BuildCraftSilicon.redstoneChipset,1,3) }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,0), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					null,
					new ItemStack(BuildCraftSilicon.redstoneChipset,1,3),
					null,
					new ItemStack(Item.diamond,1,0),
					null,
					new ItemStack(Item.diamond,1,0),
					null,
					null,
					null }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,2), null));
		
		if(!BuildCraftFactory.hopperDisabled) {
			recipes.add(new SolderingStationRecipe(new ItemStack[]{
						new ItemStack(BuildCraftFactory.hopperBlock,1),
						new ItemStack(BuildCraftFactory.hopperBlock,1),
						new ItemStack(BuildCraftFactory.hopperBlock,1),
						new ItemStack(BuildCraftFactory.hopperBlock,1),
						null,
						new ItemStack(BuildCraftFactory.hopperBlock,1),
						new ItemStack(BuildCraftFactory.hopperBlock,1),
						new ItemStack(BuildCraftFactory.hopperBlock,1),
						new ItemStack(BuildCraftFactory.hopperBlock,1) }, 
					new ItemStack(LogisticsPipes.LogisticsParts,1,3), null));
		}

		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					new ItemStack(Block.hopperBlock,1),
					new ItemStack(Block.hopperBlock,1),
					new ItemStack(Block.hopperBlock,1),
					new ItemStack(Block.hopperBlock,1),
					null,
					new ItemStack(Block.hopperBlock,1),
					new ItemStack(Block.hopperBlock,1),
					new ItemStack(Block.hopperBlock,1),
					new ItemStack(Block.hopperBlock,1) }, 
				new ItemStack(LogisticsPipes.LogisticsParts,1,3), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					null,
					new ItemStack(LogisticsPipes.LogisticsParts,1,3),
					null,
					null,
					new ItemStack(BuildCraftSilicon.redstoneChipset,1,3),
					null,
					null,
					new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2,1),null }, 
				new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk3,1), null));

		recipes.add(new SolderingStationRecipe(new ItemStack[]{
					new ItemStack(Block.netherBrick,1),
					new ItemStack(BuildCraftSilicon.redstoneChipset,1,3),
					new ItemStack(Block.netherBrick,1),
					null,
					new ItemStack(LogisticsPipes.LogisticsBasicPipe,1),
					null,
					new ItemStack(Block.netherBrick,1),
					null,
					new ItemStack(Block.netherBrick,1) }, 
				new ItemStack(LogisticsPipes.LogisticsFirewallPipe,1), null));
		
		recipes.add(new SolderingStationRecipe(new ItemStack[]{
				new ItemStack(Block.stoneBrick,1),
				new ItemStack(Block.workbench,1),
				new ItemStack(Block.stoneBrick,1),
				new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2,1),
				new ItemStack(Block.hopperBlock,1),
				new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2,1),
				new ItemStack(BuildCraftSilicon.redstoneChipset,1,3),
				new ItemStack(Block.chest,3),
				new ItemStack(BuildCraftSilicon.redstoneChipset,1,3)}, 
			new ItemStack(LogisticsPipes.logisticsRequestTable,1), null));
	}
	
	public static List<SolderingStationRecipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}
}
