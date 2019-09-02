/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

import logisticspipes.utils.Color;

public class SmallGuiButton extends GuiButton {

	private final int stringOffset;

	public SmallGuiButton(int buttonId, int x, int y, int width, int height, String label) {
		this(buttonId, x, y, width, height, label, 0);
	}

	public SmallGuiButton(int buttonId, int x, int y, int width, int height, String label, int offset) {
		super(buttonId, x, y, width, height, label);
		stringOffset = offset;
	}

	public SmallGuiButton(int i, int j, int k, String s) {
		super(i, j, k, s);
		stringOffset = 0;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void drawButton(@Nonnull Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (!visible) {
			return;
		}
		FontRenderer fontrenderer = minecraft.fontRenderer;
		minecraft.renderEngine.bindTexture(GuiButton.BUTTON_TEXTURES);
		// GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, minecraft.renderEngine.getTexture("/gui/gui.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		boolean flag = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		int k = getHoverState(flag);

		drawTexturedModalRect(x, y, 0, 46 + k * 20, width / 2, height / 2);
		drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height / 2);

		drawTexturedModalRect(x, y + height / 2, 0, 46 + 25 - height + k * 20, width / 2, height / 2);
		drawTexturedModalRect(x + width / 2, y + height / 2, 200 - width / 2, 46 + 25 - height + k * 20, width / 2, height / 2);

		mouseDragged(minecraft, mouseX, mouseY);

		int color = Color.getValue(Color.LIGHTER_GREY);
		if (!enabled) {
			color = Color.getValue(Color.GREY);
		} else if (flag) {
			color = Color.getValue(Color.LIGHT_YELLOW);
		}
		drawCenteredString(fontrenderer, displayString, x + width / 2, y + (height - 8) / 2 + stringOffset, color);
	}

}
