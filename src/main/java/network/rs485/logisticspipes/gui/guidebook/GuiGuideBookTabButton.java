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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import lombok.Getter;
import lombok.Setter;
import static network.rs485.logisticspipes.gui.guidebook.GuiGuideBook.GUI_BOOK_TEXTURE;

public class GuiGuideBookTabButton extends GuiButton {

	private final int[] colors = { 0xE9ECEC, 0xF07613, 0xBD44B3, 0x3AAFD9, 0xF8C627, 0x70B919, 0xED8DAC, 0x3E4447, 0x8E8E86, 0x158991, 0x792AAC, 0x35399D, 0x724728, 0x546D1B, 0xA12722, 0x141519 };

	public boolean isActive;
	@Getter
	@Setter
	private SavedPage tab;

	public GuiGuideBookTabButton(int buttonId, int x, int y, SavedPage tab) {
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
		this.tab.cycleColor(false);
		playPressSound(Minecraft.getMinecraft().getSoundHandler());
	}

	public void cycleColorInverted() {
		if (isActive) return;
		this.tab.cycleColor(true);
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
		GuiGuideBook.drawStretchingSquare(x, drawY - drawHeight, x + width, drawY, 15, 40, 64, 40 + width, 64 + drawHeight, true, this.isActive ? 0xFFFFFF : colors[tab.getColor()]);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

	}
}
