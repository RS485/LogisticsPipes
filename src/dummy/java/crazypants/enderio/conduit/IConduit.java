package crazypants.enderio.conduit;

import net.minecraft.util.EnumFacing;

import com.enderio.core.common.util.BlockCoord;

public interface IConduit {

	ConnectionMode getConnectionMode(EnumFacing dir);

	BlockCoord getLocation();
}
