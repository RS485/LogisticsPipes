package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.recipes.CraftingParts;

public interface IThaumCraftProxy {

	public boolean isScannedObject(ItemStack stack, String playerName);

	public List<String> getListOfTagsForStack(ItemStack stack);

	public @SideOnly(Side.CLIENT) void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui);

	public @SideOnly(Side.CLIENT) void renderAspectsInGrid(List<String> eTags, int x, int y, int legnth, int width, GuiScreen gui);

	public abstract void addCraftingRecipes(CraftingParts parts);
}
