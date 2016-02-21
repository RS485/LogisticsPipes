package logisticspipes.pipes.upgrades.connection;

import logisticspipes.pipes.upgrades.ConnectionUpgrade;

import net.minecraft.util.EnumFacing;

public class ConnectionUpgradeDOWN extends ConnectionUpgrade {

	@Override
	public EnumFacing getSide() {
		return EnumFacing.DOWN;
	}

}
