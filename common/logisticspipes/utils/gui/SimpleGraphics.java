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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import logisticspipes.utils.Color;

/**
 * Utils class for simple drawing methods.
 */
@SideOnly(Side.CLIENT)
public final class SimpleGraphics {

	private SimpleGraphics() {}

	/**
	 * Takes colors as enum values from {@link logisticspipes.utils.Color}.
	 *
	 * @see #drawHorizontalLine(int, int, int, int, int)
	 */
	public static void drawHorizontalLine(int x1, int x2, int y, Color color, int thickness) {
		SimpleGraphics.drawHorizontalLine(x1, x2, y, Color.getValue(color), thickness);
	}

	/**
	 * Draws a horizontal line from x1 to x2.
	 *
	 * @param x1        the start coordinate
	 * @param x2        the end coordinate
	 * @param y         the y-coordinate the line is on
	 * @param color     the color, which the line will have
	 * @param thickness the thickness, which the line will have
	 * @see net.minecraft.client.gui.Gui#drawHorizontalLine(int, int, int, int)
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
	 * Takes colors as enum values from {@link logisticspipes.utils.Color}.
	 *
	 * @see #drawVerticalLine(int, int, int, int, int)
	 */
	public static void drawVerticalLine(int x, int y1, int y2, Color color, int thickness) {
		SimpleGraphics.drawVerticalLine(x, y1, y2, Color.getValue(color), thickness);
	}

	/**
	 * Draws a vertical line from y1 to y2.
	 *
	 * @param x         the x-coordinate the line is on
	 * @param y1        the start coordinate
	 * @param y2        the end coordinate
	 * @param color     the color, which the line will have
	 * @param thickness the thickness, which the line will have
	 * @see net.minecraft.client.gui.Gui#drawVerticalLine(int, int, int, int)
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
	 * Takes colors as enum values from {@link logisticspipes.utils.Color}.
	 *
	 * @see #drawRectNoBlend(int, int, int, int, int, double)
	 */
	public static void drawRectNoBlend(int x1, int y1, int x2, int y2, Color color, double zLevel) {
		SimpleGraphics.drawRectNoBlend(x1, y1, x2, y2, Color.getValue(color), zLevel);
	}

	/**
	 * Draws a solid color rectangle with the specified coordinates and color.
	 * This variation does not use GL_BLEND.
	 *
	 * @param x1     the first x-coordinate of the rectangle
	 * @param y1     the first y-coordinate of the rectangle
	 * @param x2     the second x-coordinate of the rectangle
	 * @param y2     the second y-coordinate of the rectangle
	 * @param color  the color of the rectangle
	 * @param zLevel the z-level of the graphic
	 * @see net.minecraft.client.gui.Gui#drawRect(int, int, int, int, int)
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

		// no blend //GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		// no blend //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GL11.glColor4f(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		buf.pos(x1, y2, zLevel).endVertex();
		buf.pos(x2, y2, zLevel).endVertex();
		buf.pos(x2, y1, zLevel).endVertex();
		buf.pos(x1, y1, zLevel).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
		// no blend //GlStateManager.disableBlend();
	}

	/**
	 * Takes colors as enum values from {@link logisticspipes.utils.Color}.
	 *
	 * @see #drawGradientRect(int, int, int, int, int, int, double)
	 */
	public static void drawGradientRect(int x1, int y1, int x2, int y2, Color colorA, Color colorB, double zLevel) {
		SimpleGraphics.drawGradientRect(x1, y1, x2, y2, Color.getValue(colorA), Color.getValue(colorB), zLevel);
	}

	/**
	 * Draws a rectangle with a vertical gradient between the specified colors.
	 *
	 * @param x1     the first x-coordinate of the rectangle
	 * @param y1     the first y-coordinate of the rectangle
	 * @param x2     the second x-coordinate of the rectangle
	 * @param y2     the second y-coordinate of the rectangle
	 * @param colorA the first color, starting from y1
	 * @param colorB the second color, ending in y2
	 * @param zLevel the z-level of the graphic
	 * @see net.minecraft.client.gui.Gui#drawGradientRect(int, int, int, int,
	 * int, int)
	 */
	public static void drawGradientRect(int x1, int y1, int x2, int y2, int colorA, int colorB, double zLevel) { // TODO
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(7425);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buf.pos(x2, y1, zLevel).color(Color.getRed(colorA), Color.getGreen(colorA), Color.getBlue(colorA), Color.getAlpha(colorA)).endVertex();
		buf.pos(x1, y1, zLevel).color(Color.getRed(colorA), Color.getGreen(colorA), Color.getBlue(colorA), Color.getAlpha(colorA)).endVertex();
		buf.pos(x1, y2, zLevel).color(Color.getRed(colorB), Color.getGreen(colorB), Color.getBlue(colorB), Color.getAlpha(colorB)).endVertex();
		buf.pos(x2, y2, zLevel).color(Color.getRed(colorB), Color.getGreen(colorB), Color.getBlue(colorB), Color.getAlpha(colorB)).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	/**
	 * Draws a textured rectangle.
	 *
	 * @param x      the x-coordinate of the rectangle
	 * @param y      the y-coordinate of the rectangle
	 * @param u      the u-coordinate of the texture
	 * @param v      the v-coordinate of the texture
	 * @param width  the width of the rectangle
	 * @param height the height of the rectangle
	 * @param zLevel the z-level of the graphic
	 * @see net.minecraft.client.gui.Gui#drawTexturedModalRect(int, int, int,
	 * int, int, int)
	 */
	public static void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, double zLevel) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, y + height, zLevel).tex(u * f, (v + height) * f1);
		buf.pos(x + width, y + height, zLevel).tex((u + width) * f, (v + height) * f1);
		buf.pos(x + width, y, zLevel).tex((u + width) * f, v * f1);
		buf.pos(x, y, zLevel).tex(u * f, v * f1);
		tessellator.draw();
	}

	/**
	 * Draws the specified string with a z-translated drop shadow.
	 *
	 * @param fontRenderer the font renderer to render the string with
	 * @param s            the string to render
	 * @param x            the x-coordinate of the string
	 * @param y            the y-coordinate of the string
	 * @param color        the color of the string
	 * @return the stop x-coordinate of the drawn string
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

	/**
	 * Takes colors as enum values from {@link logisticspipes.utils.Color}.
	 *
	 * @see #drawQuad(Tessellator, int, int, int, int, int, double)
	 */
	public static void drawQuad(Tessellator tessellator, int x, int y, int width, int height, Color color, double zLevel) {
		SimpleGraphics.drawQuad(tessellator, x, y, width, height, Color.getValue(color), zLevel);
	}

	/**
	 * Adds a quad to the tesselator at the specified position with the set
	 * width and height and color.
	 *
	 * @param tessellator the tesselator
	 * @param x           the x-coordinate of the quad
	 * @param y           the y-coordinate of the quad
	 * @param width       the width of the quad
	 * @param height      the height of the quad
	 * @param color       the color of the quad
	 * @param zLevel      the z-level of the quad
	 */
	public static void drawQuad(Tessellator tessellator, int x, int y, int width, int height, int color, double zLevel) {
		BufferBuilder buf = tessellator.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buf.pos(x, y, zLevel).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
		buf.pos(x, y + height, zLevel).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
		buf.pos(x + width, y + height, zLevel).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
		buf.pos(x + width, y, zLevel).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
		tessellator.draw();
	}
}
