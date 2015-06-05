package logisticspipes.proxy.interfaces;

import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;

import net.minecraft.tileentity.TileEntity;

public interface ICoFHPowerProxy {

	public boolean isEnergyReceiver(TileEntity tile);

	public ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile);

	public void addCraftingRecipes(ICraftingParts parts);

	public ICoFHEnergyStorage getEnergyStorage(int i);

	public boolean isAvailable();
}
