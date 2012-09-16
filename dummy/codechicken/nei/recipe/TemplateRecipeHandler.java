package codechicken.nei.recipe;

import java.util.ArrayList;

public abstract class TemplateRecipeHandler implements ICraftingHandler, IUsageHandler {
	public abstract class CachedRecipe {}
	public ArrayList<CachedRecipe> arecipes = new ArrayList<CachedRecipe>();
}
