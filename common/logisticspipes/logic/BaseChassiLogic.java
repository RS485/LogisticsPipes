package logisticspipes.logic;

import net.minecraft.src.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.network.TileNetworkData;

public class BaseChassiLogic extends BaseRoutingLogic {
	
	@TileNetworkData
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	
	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {}

	@Override
	public void destroy() {}

}
