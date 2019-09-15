package logisticspipes.asm;

import net.minecraft.block.entity.BlockEntity;

import lombok.SneakyThrows;

import logisticspipes.LPConstants;
import logisticspipes.routing.pathfinder.changedetection.TEControl;

public class LogisticsASMHookClass {

	public static void callingClearedMethod() {
		throw new RuntimeException("This Method should never be called");
	}

	@SneakyThrows(Exception.class)
	public static void validate(BlockEntity tile) {
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
	public static void invalidate(BlockEntity tile) {
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
