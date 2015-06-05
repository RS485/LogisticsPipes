package logisticspipes.pipes.upgrades.connection;

import logisticspipes.pipes.upgrades.ConnectionUpgrade;

import net.minecraftforge.common.util.ForgeDirection;

public class ConnectionUpgradeDOWN extends ConnectionUpgrade {

	@Override
	public ForgeDirection getSide() {
		return ForgeDirection.DOWN;
	}

}
