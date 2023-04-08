/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils;

/**
 * Enumeration for colors and their int values. Also contains some static
 * functions.
 */
public enum Color {
	BLANK(0x00000000),
	WHITE(0xFFFFFFFF),
	WHITE_50(0x80FFFFFF),
	BLACK(0xFF000000),
	LIGHTER_GREY(0xFFE0E0E0),
	LIGHT_GREY(0xFFC0C0C0),
	GREY(0xFFA0A0A0),
	DARK_GREY(0xFF808080),
	DARKER_GREY(0xFF555555),
	RED(0xFFFF0000),
	GREEN(0xFF00FF00),
	BLUE(0xFF0000FF),
	LIGHT_YELLOW(0xFFFFFFA0),
	TEXT_DARK(0xff404040),
	TEXT_WHITE(0xffffffff),
	TEXT_HOVERED(0xffffffa0),
	TEXT_DISABLED(0xffA0A0A0),
	BACKGROUND_LIGHT(0xffc6c6c6),
	BACKGROUND_DARK(0xff8b8b8b),
	/** Light red */
	FUZZY_ORE_DICT_COLOR(0xFFFF4040),
	/** Light green */
	FUZZY_IGNORE_DAMAGE_COLOR(0xFF40FF40),
	/** Bright blue */
	FUZZY_IGNORE_NBT_COLOR(0xFF4040FF),
	/** Muddy Green */
	FUZZY_ORE_CATEGORY_COLOR(0xFF7F7F40),
	;

	private final int colorValue;

	Color(int value) {
		colorValue = value;
	}

	public int getValue() {
		return colorValue;
	}

	public static int getValue(Color color) {
		return color.colorValue;
	}

	public static float getAlpha(int colorValue) {
		return (colorValue >> 24 & 255) / 255.0F;
	}

	public static float getRed(int colorValue) {
		return (colorValue >> 16 & 255) / 255.0F;
	}

	public static float getGreen(int colorValue) {
		return (colorValue >> 8 & 255) / 255.0F;
	}

	public static float getBlue(int colorValue) {
		return (colorValue & 255) / 255.0F;
	}
}
