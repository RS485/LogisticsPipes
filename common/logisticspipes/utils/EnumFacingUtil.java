package logisticspipes.utils;

import net.minecraft.util.EnumFacing;

public class EnumFacingUtil {

	public static EnumFacing getOrientation(int input) {
		if (input < 0 || EnumFacing.VALUES.length <= input) {
			return null;
		}
		return EnumFacing.VALUES[input];
	}
}
