package logisticspipes.proxy.cofh.subproxies;

import net.minecraft.util.EnumFacing;

public interface ICoFHEnergyReceiver {

	public int getMaxEnergyStored();

	public int getEnergyStored();

	public int receiveEnergy(EnumFacing opposite, int i, boolean b);

}
