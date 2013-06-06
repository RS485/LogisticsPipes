package codechicken.nei.recipe;

import java.util.ArrayList;

import codechicken.nei.PositionedStack;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public interface IRecipeHandler {
	boolean hasOverlay(GuiContainer gui, Container container, int recipe);
	public ArrayList<PositionedStack> getIngredientStacks(int recipeIndex);
}
