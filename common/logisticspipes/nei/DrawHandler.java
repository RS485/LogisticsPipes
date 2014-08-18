package logisticspipes.nei;

import java.util.List;

import codechicken.nei.guihook.IContainerDrawHandler;
import logisticspipes.utils.QuickSortChestMarkerStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class DrawHandler implements IContainerDrawHandler {

    private static final ResourceLocation WIDGITS      = new ResourceLocation("textures/gui/widgets.png");
    
	@Override
	public void onPreDraw(GuiContainer gui) {}
	
	@Override
	public void renderObjects(GuiContainer gui, int mousex, int mousey) {
	}
	
	@Override
	public void postRenderObjects(GuiContainer gui, int mousex, int mousey) {}
	
	@Override
	@SuppressWarnings("unchecked")
	public void renderSlotUnderlay(GuiContainer gui, Slot slotActive) {
		if(slotActive.slotNumber == 0) {
			if(QuickSortChestMarkerStorage.getInstance().isActivated()) {
				for(Slot slot: ((List<Slot>) gui.inventorySlots.inventorySlots)) {
					if(QuickSortChestMarkerStorage.getInstance().getMarker().contains(slot.slotNumber)) {
						Minecraft.getMinecraft().renderEngine.bindTexture(WIDGITS);
						gui.drawTexturedModalRect(slot.xDisplayPosition - 3, slot.yDisplayPosition - 3, 1, 23, 22, 22);
					}
				}
			}
		}
	}
	
	@Override
	public void renderSlotOverlay(GuiContainer gui, Slot slot) {}
}
