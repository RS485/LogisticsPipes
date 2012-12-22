package logisticspipes.pipes.upgrades.connection;

import net.minecraftforge.common.ForgeDirection;
import logisticspipes.pipes.upgrades.ConnectionUpgrade;

public class ConnectionUpgradeNORTH extends ConnectionUpgrade {

	@Override
	public ForgeDirection getSide() {
		return ForgeDirection.NORTH;
	}

}
