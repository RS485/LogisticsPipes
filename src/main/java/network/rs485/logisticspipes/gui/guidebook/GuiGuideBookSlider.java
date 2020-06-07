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
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import lombok.Getter;
import static network.rs485.logisticspipes.gui.guidebook.GuiGuideBook.GUI_BOOK_TEXTURE;
import org.jetbrains.annotations.NotNull;

import network.rs485.logisticspipes.util.math.Rectangle;

public class GuiGuideBookSlider extends GuiButton {

	@Getter
	private float progress;
	private boolean dragging;
	public int top, bot;

	public void setProgress(float progress) {
		this.progress = progress;
		this.y = MathHelper.clamp(this.top + (int) ((this.bot - this.top) * progress), this.top, this.bot);
	}

	public void setProgress(int y) {
		this.y = MathHelper.clamp(y, this.top, this.bot);
		this.progress = (1.0F * (this.y - this.top)) / (this.bot - this.top);
	}

	public GuiGuideBookSlider(int buttonId, int x, int yTop, int yBot, float z, float progress, int widthIn, int heightIn) {
		super(buttonId, x, 0, widthIn, heightIn, "");
		this.enabled = true;
		this.zLevel = z;
		this.top = yTop;
		this.bot = yBot - heightIn;
		this.setProgress(progress);
	}

	public void reset() {
		this.setProgress(0.0F);
	}

	@Override
	public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) return;
		mc.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		boolean btnAtlasOffsetY = this.hovered && !dragging || !this.enabled;
		boolean btnAtlasOffsetX = this.dragging || !this.enabled;
		GuiGuideBook.drawStretchingSquare(this.x, this.y, this.x + this.width, this.y + this.height, (int) zLevel, (double) (96 + ((btnAtlasOffsetX ? 1 : 0) * 12)), (double) ((0 + (btnAtlasOffsetY ? 1 : 0) * 15)), (double) (108 + ((btnAtlasOffsetX ? 1 : 0) * 12)), (double) (((btnAtlasOffsetY ? 1 : 0) * 15) + 15));
		this.mouseDragged(mc, mouseX, mouseY);
	}

	@Override
	protected void mouseDragged(@NotNull Minecraft mc, int mouseX, int mouseY) {
		if (dragging) {
			setProgress((int) (mouseY - this.height / 2.0F));
		}
		super.mouseDragged(mc, mouseX, mouseY);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		if (dragging) {
			dragging = false;
		}
		super.mouseReleased(mouseX, mouseY);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			dragging = true;
		}
		return super.mousePressed(mc, mouseX, mouseY);
	}

	@Override
	public void playPressSound(@NotNull SoundHandler soundHandlerIn) {}
}
