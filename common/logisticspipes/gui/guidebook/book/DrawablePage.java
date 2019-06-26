package logisticspipes.gui.guidebook.book;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.string.StringUtils;

public class DrawablePage implements IDrawable {

	public static final float HEADER_SCALING = 1.5F;

	/*
	@Override
	public int draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, int yOffset) {
		String unformattedText = GuiGuideBook.currentPage.page.getText();
		ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent("Test");
		itextcomponent.appendText(unformattedText);
		GuiUtilRenderComponents.splitText(itextcomponent, gui.getAreaAcrossX(), gui.mc.fontRenderer, true, true);
		return 0;
	}
	*/

	@Override
	public int draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, int yOffset) {
		String unformattedText = GuiGuideBook.currentPage.page.getText();
		ArrayList<String> text = StringUtils.splitLines(unformattedText, mc.fontRenderer, gui.getAreaAcrossX());
		int areaCurrentY = 0;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, gui.getZText());
		int lastFormatIndex = 0;
		TextFormatting previousFormat = TextFormatting.RESET;
		for (String line : text) {
			if (line.indexOf("=====") > -1 || line.indexOf("-----") > -1) {
				gui.drawStretchingSquare(gui.getAreaX0(), gui.getAreaY0() + areaCurrentY + yOffset + 1, gui.getAreaX1() - 2, gui.getAreaY0() + areaCurrentY + yOffset + 2, 15, 3, 3, 4, 4);
				areaCurrentY += 6;
			} else if (line.indexOf("##") > -1) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(gui.getAreaX0(), gui.getAreaY0() + areaCurrentY + yOffset, 0);
				GlStateManager.scale(HEADER_SCALING - 0.01F, HEADER_SCALING - 0.01F, 1.0F);
				line = line.replace("##", "");
				mc.fontRenderer.drawString(previousFormat + line + TextFormatting.RESET, 0, 0, 0xFFFFFF);
				GlStateManager.popMatrix();
				areaCurrentY += (int) (10 * HEADER_SCALING);
			} else {
				mc.fontRenderer.drawString(previousFormat + line + TextFormatting.RESET, gui.getAreaX0(), gui.getAreaY0() + areaCurrentY + yOffset, 0xFFFFFF);
				lastFormatIndex = 0;
				for (TextFormatting format : TextFormatting.values()) {
					if (line.lastIndexOf(format.toString()) > lastFormatIndex) {
						previousFormat = format;
						lastFormatIndex = line.lastIndexOf(format.toString());
					}
				}
				areaCurrentY += 10;
			}
		}

		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, gui.getZTitleButtons());
		GuiGuideBook.drawPageCount(gui);
		GlStateManager.popMatrix();
		return areaCurrentY;
	}
}
