package network.rs485.logisticspipes.world;

import net.minecraft.util.EnumFacing;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SideUtils {
	public int getIntegerForFacing(EnumFacing face) {
		if(face == null) {
			return 6;
		}
		return face.ordinal();
	}
}
