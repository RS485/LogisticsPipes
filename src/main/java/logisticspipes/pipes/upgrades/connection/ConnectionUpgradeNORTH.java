package logisticspipes.pipes.upgrades.connection;

import logisticspipes.pipes.upgrades.ConnectionUpgrade;
import net.minecraftforge.common.ForgeDirection;

public class ConnectionUpgradeNORTH extends ConnectionUpgrade {

	@Override
	public ForgeDirection getSide() {
		return ForgeDirection.NORTH;
	}

}
