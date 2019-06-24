package logisticspipes.gui.guidebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LPConstants;
import logisticspipes.gui.guidebook.book.SavedTab;

public class GuiGuideBookTabButton extends GuiButton {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");
	private final int[] colors = { 0xE9ECEC, 0xF07613, 0xBD44B3, 0x3AAFD9, 0xF8C627, 0x70B919, 0xED8DAC, 0x3E4447, 0x8E8E86, 0x158991, 0x792AAC, 0x35399D, 0x724728, 0x546D1B, 0xA12722, 0x141519 };

	public boolean isActive;
	@Getter
	@Setter
	private SavedTab tab;

	public GuiGuideBookTabButton(int buttonId, int x, int y, SavedTab tab) {
		super(buttonId, x, y, 24, 24, "");
		this.tab = tab;
		this.visible = true;
		this.isActive = false;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return enabled && visible && hovered;
	}

	public void cycleColor() {
		if (isActive) return;
		this.tab.color++;
		if (this.tab.color >= colors.length) this.tab.color = 0;
		playPressSound(Minecraft.getMinecraft().getSoundHandler());
	}

	public void cycleColorInverted() {
		if (isActive) return;
		this.tab.color--;
		if (this.tab.color < 0) this.tab.color = this.colors.length - 1;
		playPressSound(Minecraft.getMinecraft().getSoundHandler());
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) return;
		mc.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
		int drawHeight = height;
		int drawY = y;
		if (!hovered && !isActive) {
			drawHeight -= 2;
		} else if (isActive) {
			drawHeight += 3;
			drawY += 3;
		}
		this.hovered = mouseX > this.x && mouseX <= this.x + this.width && mouseY >= this.y - this.height && mouseY < this.y;
		GuiGuideBook.drawStretchingSquare(x, drawY - drawHeight, x + width, drawY, 15, 40, 64, 40 + width, 64 + drawHeight, true, this.isActive ? 0xFFFFFF : colors[tab.color]);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

	}
}
