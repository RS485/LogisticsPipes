package logisticspipes.gui.guidebook.book;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import lombok.Getter;

import logisticspipes.LPConstants;
import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.GuideBookContents;
import logisticspipes.utils.string.StringUtils;

public class MenuItem {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	// Getting constants
	private final int z$titleButtons = 15; // Title and Buttons Z
	private final int z$frame = 10; // Frame Z
	private final int z$text = 5;// Text/Information Z
	private final int z$background = 0;  // Background Z

	//// Information storage
	@Getter
	private GuideBookContents.Chapter chapter;

	//// Drawing variables
	public boolean visiible, hovering, enabled;
	private int btn$bgX0, btn$bgY0, btn$bgX1, btn$bgY1;
	private int btn$x0, btn$y0, btn$x1, btn$y1, btn$x2, btn$y2, btn$x3, btn$y3;

	//// Atlas
	// Btn
	private final int atlas$btn$bgU0 = 64, atlas$btn$bgV0 = 32, atlas$btn$bgU1 = 96, atlas$btn$bgV1 = 64;
	private final int atlas$btn$u0 = 0, atlas$btn$v0 = 64, atlas$btn$u1 = 2, atlas$btn$v1 = 66, atlas$btn$u2 = 14, atlas$btn$v2 = 78, atlas$btn$u3 = 16, atlas$btn$v3 = 80;

	public MenuItem(GuideBookContents.Chapter chapter) {
		this.chapter = chapter;
		//// Drawing stuff
		this.visiible = true;
		this.hovering = false;
		this.enabled = true;
	}

	public void drawMenuItem(Minecraft mc, int mouseX, int mouseY, int x, int y, int sizeX, int sizeY, boolean text) {
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		drawMenuItemFrame(mc, mouseX, mouseY, x, y, sizeX, sizeY);
		int icon$sizeX, icon$sizeY, icon$offSetX, icon$offSetY;
		double icon$scaleX, icon$scaleY;
		icon$scaleX = 1.0;
		icon$scaleY = 1.0;
		icon$sizeX = 16 * (int) icon$scaleX;
		icon$sizeY = 16 * (int) icon$scaleY;
		icon$offSetX = (sizeX - icon$sizeX) / 2;
		icon$offSetY = (sizeY - icon$sizeY) / 2;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + icon$offSetX, y + icon$offSetY, z$text);
		GlStateManager.scale(icon$scaleX, icon$scaleY, 0);
		RenderHelper.enableGUIStandardItemLighting();
		mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(chapter.getItem()))), 0, 0);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.scale(1 / icon$scaleX, 1 / icon$scaleY, 0);
		GlStateManager.popMatrix();
	}

	public void drawMenuItemFrame(Minecraft mc, int mouseX, int mouseY, int x, int y, int sizeX, int sizeY) {
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		{
			btn$bgX0 = x + 1;
			btn$bgY0 = y + 1;
			btn$bgX1 = x + sizeX - 1;
			btn$bgY1 = y + sizeY - 1;
			btn$x0 = x;
			btn$y0 = y;
			btn$x1 = x + 2;
			btn$y1 = y + 2;
			btn$x2 = x + sizeX - 2;
			btn$y2 = y + sizeY - 2;
			btn$x3 = x + sizeX;
			btn$y3 = y + sizeY;
		}
		this.hovering = mouseX >= x && mouseX <= x + sizeX && mouseY >= y && mouseY <= y + sizeY;
		int i = this.hovering ? 1 : 0;
		int j = this.enabled ? 1 : 2;
		if (visiible) {
			// Fill: Middle
			GuiGuideBook.drawRepeatingSquare(btn$bgX0, btn$bgY0, btn$bgX1, btn$bgY1, z$text - 1, atlas$btn$bgU0, atlas$btn$bgV0 + (i * j * 32), atlas$btn$bgU1, atlas$btn$bgV1 + (i * j * 32), false);
			// Corners: TopLeft, TopRight, BottomLeft & BottomRight
			GuiGuideBook.drawStretchingSquare(btn$x0, btn$y0, btn$x1, btn$y1, z$text, atlas$btn$u0, atlas$btn$v0 + (i * j * 16), atlas$btn$u1, atlas$btn$v1 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btn$x2, btn$y0, btn$x3, btn$y1, z$text, atlas$btn$u2, atlas$btn$v0 + (i * j * 16), atlas$btn$u3, atlas$btn$v1 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btn$x0, btn$y2, btn$x1, btn$y3, z$text, atlas$btn$u0, atlas$btn$v2 + (i * j * 16), atlas$btn$u1, atlas$btn$v3 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btn$x2, btn$y2, btn$x3, btn$y3, z$text, atlas$btn$u2, atlas$btn$v2 + (i * j * 16), atlas$btn$u3, atlas$btn$v3 + (i * j * 16));
			// Edges: Top, Bottom, Left & Right
			GuiGuideBook.drawStretchingSquare(btn$x1, btn$y0, btn$x2, btn$y1, z$text, atlas$btn$u1, atlas$btn$v0 + (i * j * 16), atlas$btn$u2, atlas$btn$v1 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btn$x1, btn$y2, btn$x2, btn$y3, z$text, atlas$btn$u1, atlas$btn$v2 + (i * j * 16), atlas$btn$u2, atlas$btn$v3 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btn$x0, btn$y1, btn$x1, btn$y2, z$text, atlas$btn$u0, atlas$btn$v1 + (i * j * 16), atlas$btn$u1, atlas$btn$v2 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btn$x2, btn$y1, btn$x3, btn$y2, z$text, atlas$btn$u2, atlas$btn$v1 + (i * j * 16), atlas$btn$u3, atlas$btn$v2 + (i * j * 16));
		}

	}

	public void drawTitle(Minecraft mc, int mouseX, int mouseY){
		drawTitle(mc, mouseX, mouseY, false);
	}
	public void drawTitle(Minecraft mc, int mouseX, int mouseY, boolean above){
		if (hovering) {
			GuiGuideBook.drawBoxedCenteredString(mc, chapter.getTitle(), mouseX, above?btn$y0-19:btn$y3+ 1, 20);
		}
	}

	public void playPressSound(SoundHandler soundHandlerIn) {
		soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	public boolean mousePressed() {
		return this.enabled && this.hovering;
	}
}
