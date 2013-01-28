package logisticspipes.proxy.interfaces;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import thaumcraft.api.EnumTag;
import thaumcraft.api.ObjectTags;

public interface IThaumCraftProxy {
	public void renderAspectAt(EnumTag etag, int x, int y, GuiScreen gui);
	public ObjectTags getTagsForStack(ItemStack stack);
	public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui);
}
