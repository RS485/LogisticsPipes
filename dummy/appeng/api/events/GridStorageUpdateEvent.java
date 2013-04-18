package appeng.api.events;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.WorldCoord;
import appeng.api.me.util.IGridInterface;

/**
 * Posted when crafting options in a AE Network update.
 */
public class GridStorageUpdateEvent extends WorldEvent {
	
	final public WorldCoord coord;
	final public IGridInterface grid;
	
	public GridStorageUpdateEvent(World world, WorldCoord wc, IGridInterface gi ) {
		super(world);
		grid = gi;
		coord = wc;
	}
	
}
