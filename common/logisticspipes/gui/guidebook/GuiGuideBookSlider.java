package logisticspipes.gui.guidebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LPConstants;

public class GuiGuideBookSlider extends GuiButton {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	@Getter
	private float progress;
	private boolean dragging;
	public int max, min;

	public void setProgress(float progress){
		this.y = (int)((this.max-this.min)*progress);
	}

	public GuiGuideBookSlider(int buttonId, int x, int yTop, int yBot, float z, float progress, int widthIn, int heightIn) {
		super(buttonId, x, yTop, widthIn, heightIn, "");
		this.progress = progress;
		this.enabled = true;
		this.zLevel = z;
		this.max = yTop;
		this.min = yBot - heightIn;
		this.y = (int) (max + (min - max) * progress);
	}

	public void reset(){
		this.y = max;
		this.progress = 0.0F;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible)
		{
			mc.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.hovered&&!dragging||!this.enabled?1:0;
			int j = this.dragging||!this.enabled?1:0;
			GuiGuideBook.drawStretchingSquare(this.x, this.y, this.x + this.width, this.y + this.height, (int) zLevel,(double) (96 + (j * 12)),(double) (0 + (i * 15)),(double) (108 + (j * 12)),(double) ((i * 15) + 15));
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if(dragging){
			this.y = (int) MathHelper.clamp(mouseY - this.height / 2.0F, this.max, this.min);
			this.progress = (1.0F * (this.y - this.max))/(this.min - this.max);
		}
		super.mouseDragged(mc, mouseX, mouseY);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		if(dragging){
			this.y = (int) MathHelper.clamp(mouseY - this.height / 2.0F, this.max, this.min);
			dragging = false;
		}
		super.mouseReleased(mouseX, mouseY);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if(super.mousePressed(mc, mouseX, mouseY)){
			dragging = true;
		}
		return super.mousePressed(mc, mouseX, mouseY);
	}

	@Override
	public void playPressSound(SoundHandler soundHandlerIn) {
		return;
	}
}
