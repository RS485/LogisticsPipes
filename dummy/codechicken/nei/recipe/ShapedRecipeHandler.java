package codechicken.nei.recipe;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import codechicken.nei.PositionedStack;

public class ShapedRecipeHandler extends TemplateRecipeHandler {

	public class CachedShapedRecipe extends CachedRecipe {
		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;
		public CachedShapedRecipe(int i, int j, Object[] object, ItemStack result) {}
	}

	public Class<? extends GuiContainer> getGuiClass(){return null;}

	public String getRecipeName(){return null;}

	public String getGuiTexture(){return null;}

	public void loadCraftingRecipes(ItemStack result) {}

	public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {return false;}

	public ArrayList<PositionedStack> getIngredientStacks(int recipeIndex) {return null;}
}
