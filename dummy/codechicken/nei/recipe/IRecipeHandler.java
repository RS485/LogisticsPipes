package codechicken.nei.recipe;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;

public interface IRecipeHandler {
	boolean hasOverlay(GuiContainer gui, Container container, int recipe);
}
