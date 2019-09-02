package logisticspipes.modplugins.nei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.nei.guihook.IContainerDrawHandler;

import logisticspipes.utils.QuickSortChestMarkerStorage;
import logisticspipes.utils.gui.GuiGraphics;

@SideOnly(Side.CLIENT)
public class DrawHandler implements IContainerDrawHandler {

	@Override
	public void renderObjects(GuiContainer gui, int mousex, int mousey) {}

	@Override
	public void postRenderObjects(GuiContainer gui, int mousex, int mousey) {}

	//TODO: Find way to replicate this with the new api
/*
	@Override
	@SuppressWarnings("unchecked")
	public void renderSlotUnderlay(GuiContainer gui, Slot slotActive) {
		if (slotActive.slotNumber == 0) {
			if (QuickSortChestMarkerStorage.getInstance().isActivated()) {
				((List<Slot>) gui.inventorySlots.inventorySlots).stream()
						.filter(slot -> QuickSortChestMarkerStorage.getInstance().getMarker().contains(slot.slotNumber))
						.forEach(slot -> {
							Minecraft.getMinecraft().renderEngine.bindTexture(GuiGraphics.WIDGETS_TEXTURE);
							gui.drawTexturedModalRect(slot.xPos - 3, slot.yPos - 3, 1, 23, 22, 22);
						});
			}
		}
	}
*/
	@Override
	public void renderSlotOverlay(GuiContainer gui, Slot slotActive) {
		//TODO: Same as above
		if (slotActive.slotNumber == 0) {
			if (QuickSortChestMarkerStorage.getInstance().isActivated()) {
				gui.inventorySlots.inventorySlots.stream()
						.filter(slot -> QuickSortChestMarkerStorage.getInstance().getMarker().contains(slot.slotNumber))
						.forEach(slot -> {
							Minecraft.getMinecraft().renderEngine.bindTexture(GuiGraphics.WIDGETS_TEXTURE);
							gui.drawTexturedModalRect(slot.xPos - 3, slot.yPos - 3, 1, 23, 22, 22);
						});
			}
		}
	}
}
