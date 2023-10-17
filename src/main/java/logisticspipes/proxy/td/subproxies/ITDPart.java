package logisticspipes.proxy.td.subproxies;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITDPart {

	TileEntity getInternalDuct();

	void setWorld_LP(World world);

	void invalidate();

	void onChunkUnload();

	void scheduleNeighborChange();

	void connectionsChanged();

	boolean isLPSideBlocked(int i);

	void setPos(BlockPos pos);
}