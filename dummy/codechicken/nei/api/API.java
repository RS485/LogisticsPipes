package codechicken.nei.api;

import net.minecraft.client.gui.inventory.GuiContainer;
import codechicken.nei.MultiItemRange;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;

public class API {
	public static void addSetRange(String string, MultiItemRange main) {}

	public static void registerRecipeHandler(ICraftingHandler neiSolderingStationRecipeManager) {}

	public static void registerUsageHandler(IUsageHandler neiSolderingStationRecipeManager) {}

	public static void registerGuiOverlayHandler(Class<? extends GuiContainer> classz, IOverlayHandler handler, String ident) {}
}
