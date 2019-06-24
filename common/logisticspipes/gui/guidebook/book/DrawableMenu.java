package logisticspipes.gui.guidebook.book;

import net.minecraft.client.Minecraft;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.GuideBookContents;

public class DrawableMenu implements IDrawable {

	public DrawableMenu() {
		super();
	}

	@Override
	public int draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, int yOffset) {
		int area$currentY = 0;
		mouseX = mouseX < gui.getGuiX0() || mouseX > gui.getGuiX3() ? 0 : mouseX;
		mouseY = mouseY < gui.getGuiY0() || mouseY > gui.getGuiY3() ? 0 : mouseY;
		for (GuideBookContents.Division div : gui.gbc.getDivisions()) {
			gui.drawMenuText(mc, gui.getAreaX0(), gui.getAreaY0() + area$currentY + yOffset, gui.getAreaAcrossX(), 19, div.getTitle());
			area$currentY += 20;
			for (int chapterIndex = 0; chapterIndex < div.getChapters().size(); chapterIndex++) {
				gui.divisionsList.get(div.getDindex()).getList().get(chapterIndex).drawMenuItem(mc, mouseX, mouseY, gui.getAreaX0() + (chapterIndex % gui.getTileMax() * (gui.getTileSize() + gui.getTileSpacing())), gui.getAreaY0() + area$currentY + yOffset, gui.getTileSize(), gui.getTileSize(), false);
				int tileBottom = (gui.getAreaY0() + area$currentY + yOffset + gui.getTileSize());
				int maxBottom = gui.getAreaY1();
				boolean above = tileBottom > maxBottom;
				gui.divisionsList.get(div.getDindex()).getList().get(chapterIndex).drawTitle(mc, mouseX, mouseY, above);
				if ((chapterIndex + 1) % gui.getTileMax() == 0) area$currentY += gui.getTileSpacing() + gui.getTileSize();
				if (chapterIndex == div.getChapters().size() - 1) area$currentY += gui.getTileSize();
			}
		}
		return area$currentY;
	}
}