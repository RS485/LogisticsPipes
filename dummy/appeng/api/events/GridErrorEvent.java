package appeng.api.events;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.WorldCoord;

public class GridErrorEvent extends WorldEvent {
	
	public WorldCoord coord;
	public GridErrorEvent(World world, WorldCoord wc ) {
		super(world);
		coord = wc;
	}
	
}
