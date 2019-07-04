package logisticspipes.guiParts;

import net.minecraft.util.ResourceLocation;

import logisticspipes.utils.gui.GuiGraphics;

public class guiParts {
	public static guiPart normalSlot      = new guiPart(GuiGraphics.SLOT_TEXTURE);
	public static final ResourceLocation PLAYER_INVENTORY_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui_creator/player_inventory.png");
	public static guiPart playerInventory = new guiPart(PLAYER_INVENTORY_TEXTURE);


}
