package codechicken.nei.recipe;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import codechicken.nei.PositionedStack;

public interface IRecipeHandler {
	public String getRecipeName();
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe);
	public List<PositionedStack> getIngredientStacks(int recipe);
}
