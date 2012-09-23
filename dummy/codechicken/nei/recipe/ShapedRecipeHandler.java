package codechicken.nei.recipe;

import java.util.ArrayList;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.ItemStack;
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
}
