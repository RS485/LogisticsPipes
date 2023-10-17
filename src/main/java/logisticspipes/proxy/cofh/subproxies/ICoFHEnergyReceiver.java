package logisticspipes.proxy.cofh.subproxies;

import net.minecraft.util.EnumFacing;

public interface ICoFHEnergyReceiver {

	int getMaxEnergyStored();

	int getEnergyStored();

	int receiveEnergy(EnumFacing opposite, int i, boolean b);

}
