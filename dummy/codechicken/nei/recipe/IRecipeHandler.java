package codechicken.nei.recipe;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public interface IRecipeHandler {
	boolean hasOverlay(GuiContainer gui, Container container, int recipe);
}
