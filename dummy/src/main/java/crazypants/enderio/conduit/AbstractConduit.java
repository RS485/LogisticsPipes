package crazypants.enderio.conduit;

import net.minecraft.util.EnumFacing;

import com.enderio.core.common.util.BlockCoord;

public class AbstractConduit implements IConduit {

	@Override
	public ConnectionMode getConnectionMode(EnumFacing dir) {
		return null;
	}

	@Override
	public BlockCoord getLocation() {
		return null;
	}
}
