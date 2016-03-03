package crazypants.enderio.conduit;

import com.enderio.core.common.util.BlockCoord;
import net.minecraftforge.common.util.ForgeDirection;

public class AbstractConduit implements IConduit {
	@Override
	public ConnectionMode getConnectionMode(ForgeDirection dir) {
		return null;
	}

	@Override
	public BlockCoord getLocation() {
		return null;
	}
}
