package codechicken.nei.api;

import net.minecraft.client.gui.inventory.GuiContainer;
import codechicken.nei.MultiItemRange;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;

public class API {
	public static void addSetRange(String setname, MultiItemRange range) {}

	public static void registerRecipeHandler(ICraftingHandler handler) {}

	public static void registerUsageHandler(IUsageHandler handler) {}
	
	public static void registerGuiOverlay(Class<? extends GuiContainer> class1, String string) {}

	public static void registerGuiOverlayHandler(Class<? extends GuiContainer> classz, IOverlayHandler handler, String ident) {}
}
