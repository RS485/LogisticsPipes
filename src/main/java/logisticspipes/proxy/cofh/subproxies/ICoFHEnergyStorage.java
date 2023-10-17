package logisticspipes.proxy.cofh.subproxies;

import net.minecraft.nbt.NBTTagCompound;

public interface ICoFHEnergyStorage {

	int extractEnergy(int space, boolean b);

	int receiveEnergy(int maxReceive, boolean simulate);

	int getEnergyStored();

	int getMaxEnergyStored();

	void readFromNBT(NBTTagCompound nbt);

	void writeToNBT(NBTTagCompound nbt);
}
