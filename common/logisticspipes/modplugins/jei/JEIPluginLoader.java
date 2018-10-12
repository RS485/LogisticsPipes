package logisticspipes.modplugins.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

@JEIPlugin
public class JEIPluginLoader implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		IRecipeTransferRegistry recipeTranferRegistry = registry.getRecipeTransferRegistry();
		recipeTranferRegistry.addUniversalRecipeTransferHandler(new RecipeTransferHandler(registry.getJeiHelpers().recipeTransferHandlerHelper()));
	}
}
