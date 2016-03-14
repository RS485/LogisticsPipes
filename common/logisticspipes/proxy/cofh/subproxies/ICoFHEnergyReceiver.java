package logisticspipes.proxy.cofh.subproxies;

import net.minecraft.util.EnumFacing;

public interface ICoFHEnergyReceiver {

	public int getMaxEnergyStored(EnumFacing opposite);

	public int getEnergyStored(EnumFacing opposite);

	public boolean canConnectEnergy(EnumFacing opposite);

	public int receiveEnergy(EnumFacing opposite, int i, boolean b);

}
