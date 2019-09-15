package logisticspipes.utils;

import net.minecraft.util.math.Direction;

public class EnumFacingUtil {

	@Deprecated
	public static Direction getOrientation(int input) {
		if (input < 0 || input > 5) {
			return null;
		}
		return Direction.byId(input);
	}
}
