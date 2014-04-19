package codechicken.nei.forge;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

public interface IContainerDrawHandler {

	void onPreDraw(GuiContainer gui);

	void renderObjects(GuiContainer gui, int mousex, int mousey);

	void postRenderObjects(GuiContainer gui, int mousex, int mousey);

	void renderSlotUnderlay(GuiContainer gui, Slot slotActive);

	void renderSlotOverlay(GuiContainer gui, Slot slot);
	
}
