package net.minecraft.src.buildcraft.krapht.logic;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.TileNetworkData;

public class BaseChassiLogic extends BaseRoutingLogic {
	
	@TileNetworkData
	public Orientations orientation = Orientations.Unknown;
	
	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {}

	@Override
	public void destroy() {}

}
