package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class SneakyUpgrade implements IPipeUpgrade {
	public abstract ForgeDirection getSneakyOrientation();

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return true;
	}
}
