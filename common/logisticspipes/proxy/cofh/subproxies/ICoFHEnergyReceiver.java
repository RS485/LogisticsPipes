package logisticspipes.proxy.cofh.subproxies;

import net.minecraftforge.common.util.ForgeDirection;

public interface ICoFHEnergyReceiver {

	public int getMaxEnergyStored(ForgeDirection opposite);

	public int getEnergyStored(ForgeDirection opposite);

	public boolean canConnectEnergy(ForgeDirection opposite);

	public int receiveEnergy(ForgeDirection opposite, int i, boolean b);

}
