package codechicken.nei.forge;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public interface IContainerTooltipHandler {
	public List<String> handleTooltipFirst(GuiContainer gui, int mousex, int mousey, List<String> currenttip);
	public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, List<String> currenttip);
}
