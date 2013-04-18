package appeng.api.events;

import buildcraft.energy.TileEngine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.WorldCoord;
import appeng.api.me.tiles.IGridTileEntity;

public class MultiBlockUpdateEvent extends WorldEvent {
	
	public WorldCoord coord;
	public IGridTileEntity te;
	
	public MultiBlockUpdateEvent( IGridTileEntity _te, World world, WorldCoord wc ) {
		super(world);
		coord = wc;
		te = _te;
	}
	
}
