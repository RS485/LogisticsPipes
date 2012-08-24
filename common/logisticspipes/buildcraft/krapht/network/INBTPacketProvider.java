package logisticspipes.buildcraft.krapht.network;

import net.minecraft.src.NBTTagCompound;

public interface INBTPacketProvider {
	public void readFromPacketNBT(NBTTagCompound tag);
	
	public void writeToPacketNBT(NBTTagCompound tag);
}
