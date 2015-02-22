package logisticspipes.proxy.interfaces;

import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.opencomputers.IOCTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public interface IOpenComputersProxy {

	void initLogisticsTileGenericPipe(LogisticsTileGenericPipe tile);
	
	void initLogisticsSolidTileEntity(LogisticsSolidTileEntity tile);

	void addToNetwork(TileEntity tile);

	void handleInvalidate(IOCTile tile);

	void handleChunkUnload(IOCTile tile);

	void handleWriteToNBT(IOCTile tile, NBTTagCompound nbt);

	void handleReadFromNBT(IOCTile tile, NBTTagCompound nbt);
}
