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

package network.rs485.logisticspipes.gui.guidebook.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import lombok.Getter;
import static network.rs485.logisticspipes.gui.guidebook.GuiGuideBook.GUI_BOOK_TEXTURE;

import logisticspipes.LogisticsPipes;
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook;
import network.rs485.logisticspipes.guidebook.YamlPageMetadata;

public class MenuItem {

	// Getting constants
	private final int zText = 5;

	// Information storage
	private final YamlPageMetadata metadata;
	@Getter
	private final String target;

	// Drawing variables
	public boolean visible, hovering, enabled;
	private int btnBgX0, btnBgY0, btnBgX1, btnBgY1;
	private int btnX0, btnY0, btnX1, btnY1, btnX2, btnY2, btnX3, btnY3;

	// Button atlas
	private final int btnBgAtlasU0 = 64, btnBgAtlasV0 = 32, btnBgAtlasU1 = 96, btnBgAtlasV1 = 64;
	private final int btnAtlasU0 = 0, btnAtlasV0 = 64, btnAtlasU1 = 2, btnAtlasV1 = 66, btnAtlasU2 = 14, btnAtlasV2 = 78, btnAtlasU3 = 16, btnAtlasV3 = 80;

	public MenuItem(YamlPageMetadata metadata, String target) {
		this.metadata = metadata;
		this.target = target;
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
		Item item = Item.REGISTRY.getObject(new ResourceLocation(metadata.getIcon()));
		if (LogisticsPipes.isDEBUG() && item == null) LogisticsPipes.log.error("Something is wrong with the item: " + metadata.getIcon());
		mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(item != null ? item : Items.STICK), 0, 0);
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
			//GuiGuideBook.drawRepeatingSquare(btnBgX0, btnBgY0, btnBgX1, btnBgY1, zText - 1, btnBgAtlasU0, btnBgAtlasV0 + (i * j * 32), btnBgAtlasU1, btnBgAtlasV1 + (i * j * 32), false);
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
			GuiGuideBook.drawBoxedCenteredString(mc, metadata.getTitle(), mouseX, above ? btnY0 - 19 : btnY3 + 1, 20);
		}
	}

	public void playPressSound(SoundHandler soundHandlerIn) {
		soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	public boolean mousePressed() {
		return this.enabled && this.hovering;
	}
}
