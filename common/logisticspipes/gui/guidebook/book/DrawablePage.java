package logisticspipes.gui.guidebook.book;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.string.StringUtils;

public class DrawablePage implements IDrawable {
	@Override
	public int draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, int yOffset) {
		String unformattedText = GuiGuideBook.currentPage.page.getText();
		ArrayList<String> text = StringUtils.splitLines(unformattedText, mc.fontRenderer, gui.getAreaAcrossX());
		int area$currentY = 0;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, gui.getZText());
		for (String line : text) {
			mc.fontRenderer.drawString(line, gui.getAreaX0(), gui.getAreaY0() + area$currentY + yOffset, 0xFFFFFF);
			area$currentY += 10;
		}
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, gui.getZTitleButtons());
		GuiGuideBook.drawPageCount(gui);
		GlStateManager.popMatrix();
		return area$currentY;
	}
}
