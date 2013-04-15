package appeng.api.events;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.WorldCoord;
import appeng.api.me.tiles.IGridTileEntity;

public class GridTileConnectivityEvent extends WorldEvent {
	
	public WorldCoord coord;
	public IGridTileEntity te;
	
	public GridTileConnectivityEvent( IGridTileEntity _te, World world, WorldCoord wc ) {
		super(world);
		te = _te;
		coord = wc;
	}
	
}
