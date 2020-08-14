package logisticspipes.modplugins.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

@JEIPlugin
public class JEIPluginLoader implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
		recipeTransferRegistry.addUniversalRecipeTransferHandler(new RecipeTransferHandler(registry.getJeiHelpers().recipeTransferHandlerHelper()));
		registry.addGhostIngredientHandler(LogisticsBaseGuiScreen.class, new GhostIngredientHandler());
		registry.addAdvancedGuiHandlers(new AdvancedGuiHandler());
	}
}
