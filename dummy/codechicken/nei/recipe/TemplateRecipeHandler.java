package codechicken.nei.recipe;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import codechicken.nei.PositionedStack;

public abstract class TemplateRecipeHandler implements ICraftingHandler, IUsageHandler {
	public abstract class CachedRecipe {}
	public static class RecipeTransferRect {
		public RecipeTransferRect(Rectangle rectangle, String outputId, Object... results) {}
	}
	public ArrayList<CachedRecipe> arecipes = new ArrayList<CachedRecipe>();
	public LinkedList<RecipeTransferRect> transferRects = new LinkedList<RecipeTransferRect>();
	public void loadTransferRects(){}
	public void loadCraftingRecipes(String outputId, Object... results){}
	public void loadCraftingRecipes(ItemStack result){}
	public void loadUsageRecipes(ItemStack ingredient){}
	public abstract String getGuiTexture();
	public Class<? extends GuiContainer> getGuiClass(){return null;}
	public List<PositionedStack> getIngredientStacks(int recipe) {return null;}
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {return false;}
}
