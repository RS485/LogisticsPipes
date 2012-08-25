package logisticspipes.logic;

import net.minecraft.src.EntityPlayer;
import buildcraft.api.core.Orientations;
import buildcraft.core.network.TileNetworkData;

public class BaseChassiLogic extends BaseRoutingLogic {
	
	@TileNetworkData
	public Orientations orientation = Orientations.Unknown;
	
	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {}

	@Override
	public void destroy() {}

}
