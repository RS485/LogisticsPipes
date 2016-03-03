package crazypants.enderio.conduit.item;

import com.enderio.core.common.util.BlockCoord;
import crazypants.enderio.conduit.ConnectionMode;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemConduit implements IItemConduit {
	@Override
	public ConnectionMode getConnectionMode(ForgeDirection dir) {
		return null;
	}

	@Override
	public BlockCoord getLocation() {
		return null;
	}
}
