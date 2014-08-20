package logisticspipes.proxy.interfaces;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.nbt.NBTTagCompound;

public interface IOpenComputersProxy {

	void initLogisticsTileGenericPipe(LogisticsTileGenericPipe tile);

	void addToNetwork(LogisticsTileGenericPipe tile);

	void handleLPInvalidate(LogisticsTileGenericPipe tile);

	void handleLPChunkUnload(LogisticsTileGenericPipe tile);

	void handleLPWriteToNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt);

	void handleLPReadFromNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt);
}
