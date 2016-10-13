package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import logisticspipes.recipes.CraftingParts;

public interface IThaumCraftProxy {

	boolean isScannedObject(ItemStack stack, String playerName);

	List<String> getListOfTagsForStack(ItemStack stack);

	@SideOnly(Side.CLIENT) void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui);

	@SideOnly(Side.CLIENT) void renderAspectsInGrid(List<String> eTags, int x, int y, int legnth, int width, GuiScreen gui);

	void addCraftingRecipes(CraftingParts parts);
}
