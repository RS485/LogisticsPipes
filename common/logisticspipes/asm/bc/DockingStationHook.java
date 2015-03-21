package logisticspipes.asm.bc;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class DockingStationHook {
	public static TileEntity getPipeForDockingStation(World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof LogisticsTileGenericPipe) {
			return (TileEntity) ((LogisticsTileGenericPipe) tile).tilePart.getOriginal();
		}
		return null;
	}
}
