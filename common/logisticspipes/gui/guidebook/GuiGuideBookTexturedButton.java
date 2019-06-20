package logisticspipes.gui.guidebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import com.sun.jna.platform.win32.WinUser;
import lombok.Setter;

import logisticspipes.LPConstants;

public class GuiGuideBookTexturedButton extends GuiButton {

	private int u0, v0, u1, v1, sizeX, sizeY;
	@Setter
	private boolean hasDisabledState;
	@Setter
	private EnumButtonType type;

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	public GuiGuideBookTexturedButton(int buttonId, int x, int y, int widthIn, int heightIn) {
		super(buttonId, x, y, widthIn, heightIn, "");
		this.u0 = 0;
		this.v0 = 0;
		this.u1 = 0;
		this.v1 = 0;
		this.sizeX = 0;
		this.sizeY = 0;
		this.hasDisabledState = false;
		this.type = EnumButtonType.TAB;
	}

	public GuiGuideBookTexturedButton(int buttonId, int x, int y, int widthIn, int heightIn, int u0, int v0, int z, int u1, int v1, int sizeX, int sizeY, boolean hasDisabledState, EnumButtonType type){
		super(buttonId, x, y, widthIn, heightIn, "");
		this.u0 = u0;
		this.v0 = v0;
		this.u1 = u1;
		this.v1 = v1;
		this.zLevel = z;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.hasDisabledState = hasDisabledState;
		this.type = type;
	}

	public void setBgTexture(int u0, int v0) {
		this.u0 = u0;
		this.v0 = v0;
	}

	public void setOverlayTexture(int u0, int v0, int size) {
		setOverlayTexture(u0, v0, size, size);
	}

	public void setOverlayTexture(int u0, int v0, int sizeX, int sizeY) {
		this.u1 = u0;
		this.v1 = v0;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	public void setZLevel(int z) {
		this.zLevel = z;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float ticks) {
		if(this.type == EnumButtonType.TAB){
			if (this.visible) {
				mc.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
				GuiGuideBook.drawStretchingSquare(this.x, this.y, this.x + this.width, this.y + this.height, (int) zLevel, u0, v0, u0 + this.width, v0 + this.height);
			}
			if (sizeX > 0 && sizeY > 0)
				drawTexturedButtonForegroundLayer(mc, mouseX, mouseY, GUI_BOOK_TEXTURE, u1, v1, sizeX, sizeY);
		}else if(this.type == EnumButtonType.NORMAL){
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			drawTexturedButtonForegroundLayer(mc, mouseX, mouseY, GUI_BOOK_TEXTURE, u1, v1, sizeX, sizeY);
			GlStateManager.disableAlpha();
			GlStateManager.popMatrix();
		}

	}

	public void drawTexturedButtonForegroundLayer(Minecraft mc, int mouseX, int mouseY, ResourceLocation resource, int u0, int v0, int sizeX, int sizeY) {
		if (visible) {
			mc.getTextureManager().bindTexture(resource);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = (!this.hasDisabledState && this.hovered) || (this.hasDisabledState && this.enabled && hovered)? 1 : 0;
			int j = (this.hasDisabledState && !this.enabled) ? 2 : 0;
			int offSetX = (this.width - sizeX) / 2;
			int offSetY = (this.height - sizeY) / 2;
			GuiGuideBook.drawStretchingSquare(this.x + offSetX, this.y + offSetY, this.x + offSetX + sizeX, this.y + offSetY + sizeY, (int) zLevel + 1, u0, v0 + (sizeY) * i + (sizeY) * j, u0 + sizeX, v0 + sizeY + (sizeY) * i + (sizeY) * j);
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}

	public enum EnumButtonType{
		TAB,
		NORMAL
	}
}
