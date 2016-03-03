package crazypants.enderio.conduit;

import com.enderio.core.common.util.BlockCoord;
import net.minecraftforge.common.util.ForgeDirection;

public interface IConduit {
	ConnectionMode getConnectionMode(ForgeDirection dir);
	BlockCoord getLocation();
}
