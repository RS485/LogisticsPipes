package logisticspipes.proxy.interfaces;

import net.minecraft.nbt.NBTTagCompound;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public interface IOpenComputersProxy {

	void initLogisticsTileGenericPipe(LogisticsTileGenericPipe tile);

	void addToNetwork(LogisticsTileGenericPipe tile);

	void handleLPInvalidate(LogisticsTileGenericPipe tile);

	void handleLPChunkUnload(LogisticsTileGenericPipe tile);

	void handleLPWriteToNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt);

	void handleLPReadFromNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt);
}
