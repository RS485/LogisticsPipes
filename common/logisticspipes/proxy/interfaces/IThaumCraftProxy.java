package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public interface IThaumCraftProxy {
	public void renderAspectAt(Object etag, int x, int y, GuiScreen gui);
	public Object getTagsForStack(ItemStack stack);
	public List<Integer> getListOfTagIDsForStack(ItemStack stack);
	public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui);
	public void renderAspectsInGrid(List<Integer> etagIDs, int x, int y, int legnth, int width, GuiScreen gui);
	public String getNameForTagID(int id);
}
