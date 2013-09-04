package codechicken.nei.recipe;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import codechicken.nei.PositionedStack;

public class ShapedRecipeHandler extends TemplateRecipeHandler {

    public class CachedShapedRecipe extends CachedRecipe
    {
        public ArrayList<PositionedStack> ingredients;
        public PositionedStack result;
        public CachedShapedRecipe(int width, int height, Object[] items, ItemStack out) {}
	}

    @Override
    public String getRecipeName(){return null;}

    @Override
    public String getGuiTexture(){return null;}
}
