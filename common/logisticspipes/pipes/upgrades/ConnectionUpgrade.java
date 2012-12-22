package logisticspipes.pipes.upgrades;

import net.minecraftforge.common.ForgeDirection;

public abstract class ConnectionUpgrade implements IPipeUpgrade {
	public abstract ForgeDirection getSide();

	@Override
	public boolean needsUpdate() {
		return true;
	}
}
