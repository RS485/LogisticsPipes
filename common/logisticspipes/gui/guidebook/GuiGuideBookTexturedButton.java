package logisticspipes.gui.guidebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiGuideBookTexturedButton extends GuiButton {

	public GuiGuideBookTexturedButton(int buttonId, int x, int y, int widthIn, int heightIn) {
		super(buttonId, x, y, widthIn, heightIn, "");
	}

	public void drawTexturedButton(Minecraft mc, ResourceLocation resource, int u0, int v0, int z) {
		this.zLevel = z;
		if (this.visible)
		{
			mc.getTextureManager().bindTexture(resource);
			GuiGuideBook.drawStretchingSquare(this.x, this.y, this.x + this.width, this.y + this.height, z, u0, v0, u0 + this.width, v0 + this.height);
		}
	}

	public void drawTexturedButtonForegroundLayer(Minecraft mc, int mouseX, int mouseY, ResourceLocation resource, int u0, int v0, int size) {
		if(visible){
			mc.getTextureManager().bindTexture(resource);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height - 8;
			int i = this.hovered?1:0;
			int offSetX = (this.width - size)/2;
			int offSetY = (this.height - size)/2;
			GuiGuideBook.drawStretchingSquare(this.x + offSetX, this.y + offSetY, this.x + offSetX + size, this.y + offSetY + size, (int) zLevel + 1, u0, v0 + (size) * i, u0 + size, v0 + size + (size) * i);
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}
}
