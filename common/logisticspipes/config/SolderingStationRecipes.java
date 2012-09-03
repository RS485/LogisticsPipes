package logisticspipes.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ICraftingResultHandler;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

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
		recipes.add(new SolderingStationRecipe(new ItemStack[]{new ItemStack(Item.enderPearl,1),null,new ItemStack(Item.enderPearl,1),null,new ItemStack(LogisticsPipes.ModuleItem,1,0),null,new ItemStack(Item.enderPearl,1),null,new ItemStack(Item.enderPearl,1)}, new ItemStack(LogisticsPipes.LogisticsItemCard,2,0), new ICraftingResultHandler() {
			@Override
			public void handleCrafting(ItemStack stack) {
				stack.stackTagCompound = new NBTTagCompound();
				stack.stackTagCompound.setString("UUID", UUID.randomUUID().toString());
			}
		}));
	}
	
	public static List<SolderingStationRecipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}
}
