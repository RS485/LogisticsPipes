package appeng.api.me.tiles;

import net.minecraftforge.common.ForgeDirection;

public interface IGridTeleport
{
	IGridTileEntity findRemoteSide( ForgeDirection dir );
}
