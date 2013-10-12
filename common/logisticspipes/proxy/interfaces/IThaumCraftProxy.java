package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public interface IThaumCraftProxy {
	public boolean isScannedObject(ItemStack stack, String playerName);
	public List<String> getListOfTagsForStack(ItemStack stack);
	public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui);
	public void renderAspectsInGrid(List<String> eTags, int x, int y, int legnth, int width, GuiScreen gui);
	public abstract void addCraftingRecipes();
}
