package appeng.api.me.tiles;

import net.minecraftforge.common.ForgeDirection;

/**
 * Used to signify which directions a particular IGridTileEntity or IGridMachine can connect to/from, 
 * you must implement both, if you wish to have this functionality.
 */
public interface IDirectionalMETile
{
	boolean canConnect( ForgeDirection dir );
}
