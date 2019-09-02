package logisticspipes.asm;

import logisticspipes.LPConstants;
import logisticspipes.routing.pathfinder.changedetection.TEControl;

import net.minecraft.tileentity.TileEntity;

import lombok.SneakyThrows;

public class LogisticsASMHookClass {

	public static void callingClearedMethod() {
		throw new RuntimeException("This Method should never be called");
	}

	@SneakyThrows(Exception.class)
	public static void validate(TileEntity tile) {
		try {
			TEControl.validate(tile);
		} catch (Exception e) {
			if (LPConstants.DEBUG) {
				throw e;
			}
			e.printStackTrace();
		}
	}

	@SneakyThrows(Exception.class)
	public static void invalidate(TileEntity tile) {
		try {
			TEControl.invalidate(tile);
		} catch (Exception e) {
			if (LPConstants.DEBUG) {
				throw e;
			}
			e.printStackTrace();
		}
	}
}
