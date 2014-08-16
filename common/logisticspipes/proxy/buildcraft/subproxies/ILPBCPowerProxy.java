package logisticspipes.proxy.buildcraft.subproxies;

import net.minecraftforge.common.util.ForgeDirection;

public interface ILPBCPowerProxy {

	double getMaxEnergyReceived();

	double getMaxEnergyStored();

	double getEnergyStored();

	double receiveEnergy(double d, ForgeDirection orientation);
	
}
