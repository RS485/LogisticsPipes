package logisticspipes.gui.guidebook.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static logisticspipes.gui.guidebook.GuiGuideBook.GUI_BOOK_TEXTURE;
import lombok.Getter;

import logisticspipes.LPConstants;
import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.GuideBookContents;

public class MenuItem {

	// Getting constants
	private final int zText = 5;

	// Information storage
	@Getter
	private GuideBookContents.Chapter chapter;

	// Drawing variables
	public boolean visible, hovering, enabled;
	private int btnBgX0, btnBgY0, btnBgX1, btnBgY1;
	private int btnX0, btnY0, btnX1, btnY1, btnX2, btnY2, btnX3, btnY3;

	// Button atlas
	private final int btnBgAtlasU0 = 64, btnBgAtlasV0 = 32, btnBgAtlasU1 = 96, btnBgAtlasV1 = 64;
	private final int btnAtlasU0 = 0, btnAtlasV0 = 64, btnAtlasU1 = 2, btnAtlasV1 = 66, btnAtlasU2 = 14, btnAtlasV2 = 78, btnAtlasU3 = 16, btnAtlasV3 = 80;

	public MenuItem(GuideBookContents.Chapter chapter) {
		this.chapter = chapter;
		this.visible = true;
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
		GlStateManager.translate(x + icon$offSetX, y + icon$offSetY, zText);
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
			btnBgX0 = x + 1;
			btnBgY0 = y + 1;
			btnBgX1 = x + sizeX - 1;
			btnBgY1 = y + sizeY - 1;
			btnX0 = x;
			btnY0 = y;
			btnX1 = x + 2;
			btnY1 = y + 2;
			btnX2 = x + sizeX - 2;
			btnY2 = y + sizeY - 2;
			btnX3 = x + sizeX;
			btnY3 = y + sizeY;
		}
		this.hovering = mouseX >= x && mouseX <= x + sizeX && mouseY >= y && mouseY <= y + sizeY;
		int i = this.hovering ? 1 : 0;
		int j = this.enabled ? 1 : 2;
		if (visible) {
			// Fill: Middle
			GuiGuideBook.drawRepeatingSquare(btnBgX0, btnBgY0, btnBgX1, btnBgY1, zText - 1, btnBgAtlasU0, btnBgAtlasV0 + (i * j * 32), btnBgAtlasU1, btnBgAtlasV1 + (i * j * 32), false);
			// Corners: TopLeft, TopRight, BottomLeft & BottomRight
			GuiGuideBook.drawStretchingSquare(btnX0, btnY0, btnX1, btnY1, zText, btnAtlasU0, btnAtlasV0 + (i * j * 16), btnAtlasU1, btnAtlasV1 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btnX2, btnY0, btnX3, btnY1, zText, btnAtlasU2, btnAtlasV0 + (i * j * 16), btnAtlasU3, btnAtlasV1 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btnX0, btnY2, btnX1, btnY3, zText, btnAtlasU0, btnAtlasV2 + (i * j * 16), btnAtlasU1, btnAtlasV3 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btnX2, btnY2, btnX3, btnY3, zText, btnAtlasU2, btnAtlasV2 + (i * j * 16), btnAtlasU3, btnAtlasV3 + (i * j * 16));
			// Edges: Top, Bottom, Left & Right
			GuiGuideBook.drawStretchingSquare(btnX1, btnY0, btnX2, btnY1, zText, btnAtlasU1, btnAtlasV0 + (i * j * 16), btnAtlasU2, btnAtlasV1 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btnX1, btnY2, btnX2, btnY3, zText, btnAtlasU1, btnAtlasV2 + (i * j * 16), btnAtlasU2, btnAtlasV3 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btnX0, btnY1, btnX1, btnY2, zText, btnAtlasU0, btnAtlasV1 + (i * j * 16), btnAtlasU1, btnAtlasV2 + (i * j * 16));
			GuiGuideBook.drawStretchingSquare(btnX2, btnY1, btnX3, btnY2, zText, btnAtlasU2, btnAtlasV1 + (i * j * 16), btnAtlasU3, btnAtlasV2 + (i * j * 16));
		}

	}

	public void drawTitle(Minecraft mc, int mouseX, int mouseY) {
		drawTitle(mc, mouseX, mouseY, false);
	}

	public void drawTitle(Minecraft mc, int mouseX, int mouseY, boolean above) {
		if (hovering) {
			GuiGuideBook.drawBoxedCenteredString(mc, chapter.getTitle(), mouseX, above ? btnY0 - 19 : btnY3 + 1, 20);
		}
	}

	public void playPressSound(SoundHandler soundHandlerIn) {
		soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	public boolean mousePressed() {
		return this.enabled && this.hovering;
	}
}
