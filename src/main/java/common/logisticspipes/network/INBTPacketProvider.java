package logisticspipes.network;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTPacketProvider {
	public void readFromPacketNBT(NBTTagCompound tag);
	
	public void writeToPacketNBT(NBTTagCompound tag);
}
