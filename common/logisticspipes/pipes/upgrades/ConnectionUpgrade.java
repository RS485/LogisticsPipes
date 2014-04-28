package logisticspipes.pipes.upgrades;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class ConnectionUpgrade implements IPipeUpgrade {
	public abstract ForgeDirection getSide();

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public boolean isAllowed(CoreRoutedPipe pipe) {
		return true;
	}
}
