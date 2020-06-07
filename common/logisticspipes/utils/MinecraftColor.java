package logisticspipes.utils;

import javax.annotation.Nonnull;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum MinecraftColor {
	BLACK(0xff000000),
	RED(0xffff0000),
	GREEN(0xff00ff00),
	BROWN(0xff895836),
	BLUE(0xff0000ff),
	PURPLE(0xffB064D8),
	CYAN(0xff3C8EB0),
	LIGHT_GRAY(0xffBABAC1),
	GRAY(0xff848484),
	PINK(0xffF7B4D6),
	LIME(0xff83D41C),
	YELLOW(0xffE7E72A),
	LIGHT_BLUE(0xff82ACE7),
	MAGENTA(0xffDB7AD5),
	ORANGE(0xffE69E34),
	WHITE(0xffffffff),
	BLANK(0x00000000);

	private final int colorCode;

	MinecraftColor(int colorCode) {
		this.colorCode = colorCode;
	}

	public static MinecraftColor getColor(@Nonnull ItemStack item) {
		if (!item.isEmpty()) {
			if (item.getItem() == Items.DYE && item.getItemDamage() < 16) {
				return MinecraftColor.values()[item.getItemDamage()];
			}
		}
		return BLANK;
	}

	/**
	 * Extracts the 8 bits of alpha value information from the color code and returns them as an integer.
	 * @return 8 bit (0-255) value of the Alpha channel.
	 */
	public int getAlpha(){
		return ((colorCode << 24) & 0xff);
	}

	/**
	 * Extracts the 8 bits of alpha value information from the color code and returns them as an percentage float.
	 * @return 8 bit (0-255) value of the Alpha channel as a float (0.0f - 1.0f).
	 */
	public float getAlphaF() {
		return getAlpha() / 255.0F;
	}

	/**
	 * Extracts the 8 bits of red color information from the color code and returns them as an integer.
	 * @return 8 bit (0-255) value of the Red channel.
	 */
	public int getRed(){
		return ((colorCode << 16) & 0xff);
	}

	/**
	 * Extracts the 8 bits of red color information from the color code and returns them as an percentage float.
	 * @return 8 bit (0-255) value of the Red channel as a float (0.0f - 1.0f).
	 */
	public float getRedF() {
		return getRed() / 255.0F;
	}

	/**
	 * Extracts the 8 bits of green color information from the color code and returns them as an integer.
	 * @return 8 bit (0-255) value of the Green channel.
	 */
	public int getGreen(){
		return ((colorCode << 8) & 0xff);
	}

	/**
	 * Extracts the 8 bits of green color information from the color code and returns them as an percentage float.
	 * @return 8 bit (0-255) value of the Green channel as a float (0.0f - 1.0f).
	 */
	public float getGreenF() {
		return getGreen() / 255.0F;
	}

	/**
	 * Extracts the 8 bits of blue color information from the color code and returns them as an integer.
	 * @return 8 bit (0-255) value of the Blue channel.
	 */
	public int getBlue(){
		return colorCode & 0xFF;
	}

	/**
	 * Extracts the 8 bits of blue color information from the color code and returns them as an percentage float.
	 * @return 8 bit (0-255) value of the Blue channel as a float (0.0f - 1.0f).
	 */
	public float getBlueF() {
		return getBlue() / 255.0F;
	}

	public int getColorCode() {
		return colorCode;
	}

	@Nonnull
	public ItemStack getItemStack() {
		if (this == BLANK) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(Items.DYE, 1, ordinal());
	}

	public MinecraftColor getNext() {
		if (this == BLANK) {
			return BLACK;
		}
		return MinecraftColor.values()[ordinal() + 1];
	}

	public MinecraftColor getPrev() {
		if (this == BLACK) {
			return BLANK;
		}
		return MinecraftColor.values()[ordinal() - 1];
	}
}
