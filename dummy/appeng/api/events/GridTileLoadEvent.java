package appeng.api.events;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.WorldCoord;
import appeng.api.me.tiles.IGridTileEntity;

public class GridTileLoadEvent extends WorldEvent {
	
	public WorldCoord coord;
	public IGridTileEntity te;
	
	public GridTileLoadEvent(IGridTileEntity _te, World world, WorldCoord wc ) {
		super(world);
		te = _te;
		coord = wc;
	}
	
}
