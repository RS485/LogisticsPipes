package logisticspipes.utils.string;

public enum ChatColor {
	/**
	 * Represents black
	 */
	BLACK('0'),
	/**
	 * Represents dark blue
	 */
	DARK_BLUE('1'),
	/**
	 * Represents dark green
	 */
	DARK_GREEN('2'),
	/**
	 * Represents dark blue (aqua)
	 */
	DARK_AQUA('3'),
	/**
	 * Represents dark red
	 */
	DARK_RED('4'),
	/**
	 * Represents dark purple
	 */
	DARK_PURPLE('5'),
	/**
	 * Represents gold
	 */
	GOLD('6'),
	/**
	 * Represents gray
	 */
	GRAY('7'),
	/**
	 * Represents dark gray
	 */
	DARK_GRAY('8'),
	/**
	 * Represents blue
	 */
	BLUE('9'),
	/**
	 * Represents green
	 */
	GREEN('a'),
	/**
	 * Represents aqua
	 */
	AQUA('b'),
	/**
	 * Represents red
	 */
	RED('c'),
	/**
	 * Represents light purple
	 */
	LIGHT_PURPLE('d'),
	/**
	 * Represents yellow
	 */
	YELLOW('e'),
	/**
	 * Represents white
	 */
	WHITE('f'),
	/**
	 * Represents magical characters that change around randomly
	 */
	MAGIC('k', true),
	/**
	 * Makes the text bold.
	 */
	BOLD('l', true),
	/**
	 * Makes a line appear through the text.
	 */
	STRIKETHROUGH('m', true),
	/**
	 * Makes the text appear underlined.
	 */
	UNDERLINE('n', true),
	/**
	 * Makes the text italic.
	 */
	ITALIC('o', true),
	/**
	 * Resets all previous chat colors or formats.
	 */
	RESET('r');

	/**
	 * The special character which prefixes all chat colour codes. Use this if
	 * you need to dynamically convert colour codes from your custom format.
	 */
	public static final char COLOR_CHAR = '\u00A7';

	private final char code;
	private final boolean isFormat;
	private final String toString;

	ChatColor(char code) {
		this(code, false);
	}

	ChatColor(char code, boolean isFormat) {
		this.code = code;
		this.isFormat = isFormat;
		toString = new String(new char[] { ChatColor.COLOR_CHAR, code });
	}

	/**
	 * Gets the char value associated with this color
	 *
	 * @return A char value of this color code
	 */
	public char getChar() {
		return code;
	}

	@Override
	public String toString() {
		return toString;
	}

	/**
	 * Checks if this code is a format code as opposed to a color code.
	 */
	public boolean isFormat() {
		return isFormat;
	}

	/**
	 * Checks if this code is a color code as opposed to a format code.
	 */
	public boolean isColor() {
		return !isFormat && this != RESET;
	}
}
