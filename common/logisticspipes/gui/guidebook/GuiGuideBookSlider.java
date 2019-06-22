package logisticspipes.gui.guidebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import lombok.Getter;

import logisticspipes.LPConstants;

public class GuiGuideBookSlider extends GuiButton {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	@Getter
	private float progress;
	private boolean dragging;
	public int top, bot;

	public void setProgress(float progress) {
		this.progress = progress;
		this.y = MathHelper.clamp(this.top + (int) ((this.bot - this.top) * progress), this.top, this.bot);
	}

	public void setProgress(int y) {
		this.y = MathHelper.clamp(y, this.top, this.bot);
		this.progress = (1.0F * (this.y - this.top)) / (this.bot - this.top);
	}

	public GuiGuideBookSlider(int buttonId, int x, int yTop, int yBot, float z, float progress, int widthIn, int heightIn) {
		super(buttonId, x, 0, widthIn, heightIn, "");
		this.enabled = true;
		this.zLevel = z;
		this.top = yTop;
		this.bot = yBot - heightIn;
		this.setProgress(progress);
	}

	public void reset() {
		this.setProgress(0.0F);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) return;
		mc.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		boolean btnAtlasOffsetY = this.hovered && !dragging || !this.enabled;
		boolean btnAtlasOffsetX = this.dragging || !this.enabled;
		GuiGuideBook.drawStretchingSquare(this.x, this.y, this.x + this.width, this.y + this.height, (int) zLevel, (double) (96 + ((btnAtlasOffsetX ? 1 : 0) * 12)), (double) ((0 + (btnAtlasOffsetY ? 1 : 0) * 15)), (double) (108 + ((btnAtlasOffsetX ? 1 : 0) * 12)), (double) (((btnAtlasOffsetY ? 1 : 0) * 15) + 15));
		this.mouseDragged(mc, mouseX, mouseY);
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (dragging) {
			setProgress((int) (mouseY - this.height / 2.0F));
		}
		super.mouseDragged(mc, mouseX, mouseY);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		if (dragging) {
			dragging = false;
		}
		super.mouseReleased(mouseX, mouseY);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			dragging = true;
		}
		return super.mousePressed(mc, mouseX, mouseY);
	}

	@Override
	public void playPressSound(SoundHandler soundHandlerIn) {}
}
