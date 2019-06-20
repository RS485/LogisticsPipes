package logisticspipes.gui.guidebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import lombok.Setter;

import logisticspipes.LPConstants;

public class GuiGuideBookTabButton extends GuiButton {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	public boolean isSet, isActive;
	@Setter
	private int color;

	public GuiGuideBookTabButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.visible = false;
		this.isActive = false;
		this.isSet = false;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return enabled && visible && hovered;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible)
		{
			mc.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
			int drawHeight = height;
			int drawY = y;
			if(!hovered && !isActive){
				drawHeight -= 2;
			}else if(isActive){
				drawHeight += 3;
				drawY += 3;
			}
			this.hovered = mouseX > this.x && mouseX <= this.x + this.width && mouseY >= this.y - this.height && mouseY < this.y;
			color = this.isActive?0xFFFFFF:color;
			GuiGuideBook.drawStretchingSquare(x, drawY - drawHeight, x + width, drawY, 15, 40, 64, 40 + width, 64 + drawHeight, true, color);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}
}
