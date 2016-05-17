package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.List;

public abstract class CraftingPartRecipes implements IRecipeProvider {

	public static final List<CraftingParts> craftingPartList = new ArrayList<>();

	@Override
	public final void loadRecipes() {
		loadPlainRecipes();
		craftingPartList.forEach(this::loadRecipes);
	}

	protected abstract void loadRecipes(CraftingParts parts);

	protected abstract void loadPlainRecipes();
}
