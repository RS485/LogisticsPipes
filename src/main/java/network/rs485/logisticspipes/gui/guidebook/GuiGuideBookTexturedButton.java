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
import net.minecraft.util.ResourceLocation;

import lombok.Setter;
import static network.rs485.logisticspipes.gui.guidebook.GuiGuideBook.GUI_BOOK_TEXTURE;

public class GuiGuideBookTexturedButton extends GuiButton {

	private int u0, v0, u1, v1, sizeX, sizeY;
	@Setter
	private boolean hasDisabledState;
	@Setter
	private EnumButtonType type;

	public GuiGuideBookTexturedButton(int buttonId, int x, int y, int widthIn, int heightIn) {
		this(buttonId, x, y, widthIn, heightIn, 0, 0, 20, 0, 0, 0, 0, false, EnumButtonType.TAB);
	}

	public GuiGuideBookTexturedButton(int buttonId, int x, int y, int widthIn, int heightIn, int u0, int v0, int z, int u1, int v1, int sizeX, int sizeY, boolean hasDisabledState, EnumButtonType type) {
		super(buttonId, x, y, widthIn, heightIn, "");
		this.u0 = u0;
		this.v0 = v0;
		this.u1 = u1;
		this.v1 = v1;
		this.zLevel = z;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.hasDisabledState = hasDisabledState;
		this.type = type;
	}

	public void setBgTexture(int u0, int v0) {
		this.u0 = u0;
		this.v0 = v0;
	}

	public void setOverlayTexture(int u0, int v0, int size) {
		setOverlayTexture(u0, v0, size, size);
	}

	public void setOverlayTexture(int u0, int v0, int sizeX, int sizeY) {
		this.u1 = u0;
		this.v1 = v0;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	public void setZLevel(int z) {
		this.zLevel = z;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float ticks) {
		if (this.type == EnumButtonType.TAB) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
				GuiGuideBook.drawStretchingSquare(this.x, this.y, this.x + this.width, this.y + this.height, (int) zLevel, u0, v0, u0 + this.width, v0 + this.height);
			}
			if (sizeX > 0 && sizeY > 0)
				drawTexturedButtonForegroundLayer(mc, mouseX, mouseY, GUI_BOOK_TEXTURE, u1, v1, sizeX, sizeY);
		} else if (this.type == EnumButtonType.NORMAL) {
			GlStateManager.enableAlpha();
			drawTexturedButtonForegroundLayer(mc, mouseX, mouseY, GUI_BOOK_TEXTURE, u1, v1, sizeX, sizeY);
			GlStateManager.disableAlpha();
		}

	}

	public void drawTexturedButtonForegroundLayer(Minecraft mc, int mouseX, int mouseY, ResourceLocation resource, int u0, int v0, int sizeX, int sizeY) {
		if (!this.visible) return;
		mc.getTextureManager().bindTexture(resource);
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		int i = (!this.hasDisabledState && this.hovered) || (this.hasDisabledState && this.enabled && hovered) ? 1 : 0;
		int j = (this.hasDisabledState && !this.enabled) ? 2 : 0;
		int offsetX = (this.width - sizeX) / 2;
		int offsetY = (this.height - sizeY) / 2;
		GuiGuideBook.drawStretchingSquare(this.x + offsetX, this.y + offsetY, this.x + offsetX + sizeX, this.y + offsetY + sizeY, (int) zLevel + 1, u0, v0 + (sizeY) * i + (sizeY) * j, u0 + sizeX, v0 + sizeY + (sizeY) * i + (sizeY) * j);
		this.mouseDragged(mc, mouseX, mouseY);

	}

	public enum EnumButtonType {
		TAB,
		NORMAL
	}
}
