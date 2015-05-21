/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * Utils class for simple drawing methods.
 */
public final class SimpleGraphics {

	private SimpleGraphics() {
	}

	/**
	 * Draws a horizontal line from x1 to x2.
	 *
	 * @param x1 the start coordinate
	 * @param x2 the end coordinate
	 * @param y the y-coordinate the line is on
	 * @param color the color, which the line will have
	 * @param thickness the thickness, which the line will have
	 * @see net.minecraft.client.gui.Gui method drawHorizontalLine(int, int, int, int)
	 */
	public static void drawHorizontalLine(int x1, int x2, int y, int color, int thickness) {
		if (x2 < x1) {
			int temp = x1;
			x1 = x2;
			x2 = temp;
		}

		Gui.drawRect(x1, y, x2 + 1, y + thickness, color);
	}

	/**
	 * Draws a vertical line from y1 to y2.
	 *
	 * @param x the x-coordinate the line is on
	 * @param y1 the start coordinate
	 * @param y2 the end coordinate
	 * @param color the color, which the line will have
	 * @param thickness the thickness, which the line will have
	 * @see net.minecraft.client.gui.Gui method drawVerticalLine(int, int, int, int)
	 */
	public static void drawVerticalLine(int x, int y1, int y2, int color, int thickness) {
		if (y2 < y1) {
			int temp = y1;
			y1 = y2;
			y2 = temp;
		}

		Gui.drawRect(x, y1 + 1, x + thickness, y2, color);
	}

	/**
	 * Draws a solid color rectangle with the specified coordinates and color.
	 * This variation does not use GL_BLEND.
	 *
	 * @param x1 the first x-coordinate of the rectangle
	 * @param y1 the first y-coordinate of the rectangle
	 * @param x2 the second x-coordinate of the rectangle
	 * @param y2 the second y-coordinate of the rectangle
	 * @param color the color of the rectangle
	 * @param zLevel the z-level of the graphic
	 * @see net.minecraft.client.gui.Gui method drawRect(int, int, int, int, int)
	 */
	public static void drawRectNoBlend(int x1, int y1, int x2, int y2, int color, double zLevel) {
		int temp;

		if (x1 < x2) {
			temp = x1;
			x1 = x2;
			x2 = temp;
		}

		if (y1 < y2) {
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		float alpha = (float) (color >> 24 & 255) / 255.0F;
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;

		// no blend //GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		// no blend //OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(red, green, blue, alpha);

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertex((double) x1, (double) y2, zLevel);
		tessellator.addVertex((double) x2, (double) y2, zLevel);
		tessellator.addVertex((double) x2, (double) y1, zLevel);
		tessellator.addVertex((double) x1, (double) y1, zLevel);
		tessellator.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// no blend //GL11.glDisable(GL11.GL_BLEND);
	}

	/**
	 * Draws a rectangle with a vertical gradient between the specified colors.
	 *
	 * @param x1 the first x-coordinate of the rectangle
	 * @param y1 the first y-coordinate of the rectangle
	 * @param x2 the second x-coordinate of the rectangle
	 * @param y2 the second y-coordinate of the rectangle
	 * @param colorA the first color, starting from y1
	 * @param colorB the second color, ending in y2
	 * @param zLevel the z-level of the graphic
	 * @see net.minecraft.client.gui.Gui method drawGradientRect(int, int, int, int, int, int)
	 */
	public static void drawGradientRect(int x1, int y1, int x2, int y2, int colorA, int colorB, double zLevel) {
		float alphaA = (float) (colorA >> 24 & 255) / 255.0F;
		float redA = (float) (colorA >> 16 & 255) / 255.0F;
		float greenA = (float) (colorA >> 8 & 255) / 255.0F;
		float blueA = (float) (colorA & 255) / 255.0F;
		float alphaB = (float) (colorB >> 24 & 255) / 255.0F;
		float redB = (float) (colorB >> 16 & 255) / 255.0F;
		float greenB = (float) (colorB >> 8 & 255) / 255.0F;
		float blueB = (float) (colorB & 255) / 255.0F;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		// before: GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glShadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(redA, greenA, blueA, alphaA);
		tessellator.addVertex((double) x2, (double) y1, zLevel);
		tessellator.addVertex((double) x1, (double) y1, zLevel);
		tessellator.setColorRGBA_F(redB, greenB, blueB, alphaB);
		tessellator.addVertex((double) x1, (double) y2, zLevel);
		tessellator.addVertex((double) x2, (double) y2, zLevel);
		tessellator.draw();

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Draws a textured rectangle.
	 *
	 * @param x the x-coordinate of the rectangle
	 * @param y the y-coordinate of the rectangle
	 * @param u the u-coordinate of the texture
	 * @param v the v-coordinate of the texture
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 * @param zLevel the z-level of the graphic
	 * @see net.minecraft.client.gui.Gui method drawTexturedModalRect(int, int, int, int, int, int)
	 */
	public static void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, double zLevel) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double) x, (double) (y + height), zLevel, (double) (u * f), (double) ((v + height) * f1));
		tessellator.addVertexWithUV((double) (x + width), (double) (y + height), zLevel, (double) ((u + width) * f), (double) ((v + height) * f1));
		tessellator.addVertexWithUV((double) (x + width), (double) y, zLevel, (double) ((u + width) * f), (double) (v * f1));
		tessellator.addVertexWithUV((double) x, (double) y, zLevel, (double) (u * f), (double) (v * f1));
		tessellator.draw();
	}

	/**
	 * Draws the specified string with a z-translated drop shadow.
	 *
	 * @param fontRenderer the font renderer to render the string with
	 * @param s the string to render
	 * @param x the x-coordinate of the string
	 * @param y the y-coordinate of the string
	 * @param color the color of the string
	 * @return the stop x-coordinate of the drawn string
	 * @see net.minecraft.client.gui.FontRenderer method drawString(String, int, int, int, boolean)
	 */
	public static int drawStringWithTranslatedShadow(FontRenderer fontRenderer, String s, int x, int y, int color) {
		int endX;

		// make color gray-ish and draw shadow
		int grayColor = (color & 16579836) >> 2 | color & -16777216;
		endX = fontRenderer.drawString(s, x + 1, y + 1, grayColor);

		// move to foreground and draw actual string
		GL11.glTranslated(0.0, 0.0, 1.0);
		endX = Math.max(endX, fontRenderer.drawString(s, x, y, color));
		GL11.glTranslated(0.0, 0.0, -1.0);

		return endX;
	}
}
