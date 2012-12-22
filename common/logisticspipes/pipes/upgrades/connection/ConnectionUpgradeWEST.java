package logisticspipes.pipes.upgrades.connection;

import net.minecraftforge.common.ForgeDirection;
import logisticspipes.pipes.upgrades.ConnectionUpgrade;

public class ConnectionUpgradeWEST extends ConnectionUpgrade {

	@Override
	public ForgeDirection getSide() {
		return ForgeDirection.WEST;
	}

}
