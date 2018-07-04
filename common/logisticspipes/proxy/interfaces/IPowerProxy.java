package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;

public interface IPowerProxy {

	boolean isEnergyReceiver(TileEntity tile, EnumFacing face);

	ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile, EnumFacing face);

	ICoFHEnergyStorage getEnergyStorage(int i);

	boolean isAvailable();
}
