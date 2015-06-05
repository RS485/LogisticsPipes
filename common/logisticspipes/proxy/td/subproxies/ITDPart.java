package logisticspipes.proxy.td.subproxies;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public interface ITDPart {

	TileEntity getInternalDuctForSide(ForgeDirection opposite);

	void setWorldObj_LP(World world);

	void invalidate();

	void onChunkUnload();

	void scheduleNeighborChange();

	void connectionsChanged();
}
