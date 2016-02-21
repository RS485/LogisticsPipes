package logisticspipes.proxy.td.subproxies;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface ITDPart {

	TileEntity getInternalDuctForSide(EnumFacing opposite);

	void setWorldObj_LP(World world);

	void invalidate();

	void onChunkUnload();

	void scheduleNeighborChange();

	void connectionsChanged();


}
