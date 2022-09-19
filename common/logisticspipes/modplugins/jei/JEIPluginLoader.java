package logisticspipes.modplugins.jei;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

@JEIPlugin
public class JEIPluginLoader implements IModPlugin {

	private static IJeiRuntime jeiRuntime;

	@Override
	public void register(IModRegistry registry) {
		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
		recipeTransferRegistry.addUniversalRecipeTransferHandler(new RecipeTransferHandler(registry.getJeiHelpers().recipeTransferHandlerHelper()));
		registry.addGhostIngredientHandler(LogisticsBaseGuiScreen.class, new GhostIngredientHandler());
		registry.addAdvancedGuiHandlers(new AdvancedGuiHandler());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		JEIPluginLoader.jeiRuntime = jeiRuntime;
	}

	@SideOnly(Side.CLIENT)
	public static void showRecipe(ItemStack stack) {
		jeiRuntime.getRecipesGui().show(jeiRuntime.getRecipeRegistry().createFocus(IFocus.Mode.OUTPUT, stack));
	}
}
