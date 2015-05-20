/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils.gui;

import net.minecraft.client.gui.Gui;

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
	 */
	public static void drawVerticalLine(int x, int y1, int y2, int color, int thickness) {
		if (y2 < y1) {
			int temp = y1;
			y1 = y2;
			y2 = temp;
		}

		Gui.drawRect(x, y1 + 1, x + thickness, y2, color);
	}
}
