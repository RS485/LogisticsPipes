/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.gui.guidebook;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import logisticspipes.utils.string.StringUtils;
import network.rs485.logisticspipes.gui.guidebook.book.MenuItem;

public class DrawablePage {

	public static final float HEADER_SCALING = 1.5F;

	public static int draw(Minecraft mc, SavedPage page, GuiGuideBook gui, int mouseX, int mouseY, int yOffset) {
		// Draw menu items if applicable
		int areaCurrentY = 0;
		mouseX = mouseX < gui.getGuiX0() || mouseX > gui.getGuiX3() ? 0 : mouseX;
		mouseY = mouseY < gui.getGuiY0() || mouseY > gui.getGuiY3() ? 0 : mouseY;
		if (!page.getMenuItems().isEmpty()) {
			for (GuiGuideBook.MenuItemsDivision menuItemsDivision : page.getMenuItems()) {
				GuiGuideBook.drawMenuText(mc, gui.getAreaX0(), gui.getAreaY0() + areaCurrentY + yOffset, gui.getAreaAcrossX(), 19, menuItemsDivision.name);
				areaCurrentY += 20;
				int currentTileIndex = 0;
				for (MenuItem menuItem : menuItemsDivision.getList()) {
					menuItem.drawMenuItem(mc, mouseX, mouseY, gui.getAreaX0() + ((currentTileIndex % gui.getTileMax()) * (gui.getTileSize() + gui.getTileSpacing())), gui.getAreaY0() + areaCurrentY + ((int) (currentTileIndex / (float) gui.getTileMax()) * (gui.getTileSize() + gui.getTileSpacing())) + yOffset,
							gui.getTileSize(), gui.getTileSize(), false);
					int tileBottom = (gui.getAreaY0() + areaCurrentY + yOffset + gui.getTileSize());
					int maxBottom = gui.getAreaY1();
					boolean above = tileBottom > maxBottom;
					menuItem.drawTitle(mc, mouseX, mouseY, above);
					currentTileIndex++;
				}
				areaCurrentY += (((int) (((currentTileIndex - 1) / (float) gui.getTileMax()))) * (gui.getTileSize() + gui.getTileSpacing())) + (gui.getTileSize() + gui.getTileSpacing());
			}
		}

		// Drawing the text after the menu
		String unformattedText = GuiGuideBook.currentPage.getText();
		if(!unformattedText.isEmpty()) {
			ArrayList<String> text = StringUtils.splitLines(unformattedText, mc.fontRenderer, gui.getAreaAcrossX());
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, gui.getZText());
			int lastFormatIndex = 0;
			TextFormatting previousFormat = TextFormatting.RESET;
			for (String line : text) {
				if (line.contains("=====") || line.contains("-----")) {
					GuiGuideBook.drawStretchingSquare(gui.getAreaX0(), gui.getAreaY0() + areaCurrentY + yOffset + 1, gui.getAreaX1() - 2, gui.getAreaY0() + areaCurrentY + yOffset + 2, 15, 3, 3, 4, 4);
					areaCurrentY += 6;
				} else if (line.contains("##")) {
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
			GlStateManager.popMatrix();
		}
		return areaCurrentY;
	}
}
