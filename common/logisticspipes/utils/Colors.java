package logisticspipes.utils;

import net.minecraft.item.ItemStack;

public enum Colors {
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
	
	private Colors(int colorCode) {
		this.colorCode = colorCode;
	}
	
	public int getColorCode() {
		return colorCode;
	}
	
	public ItemStack getItemStack() {
		if(this == BLANK) return null;
		return new ItemStack(351,1,ordinal());
	}
	
	public Colors getNext() {
		if(this == BLANK) return BLACK;
		return Colors.values()[ordinal() + 1];
	}
	
	public Colors getPrev() {
		if(this == BLACK) return BLANK;
		return Colors.values()[ordinal() - 1];
	}
	
	public static Colors getColor(ItemStack item) {
		if(item != null) {
			if(item.itemID == 351 && item.getItemDamage() < 16) {
				return Colors.values()[item.getItemDamage()];
			}
		}
		return BLANK;
	}
}
