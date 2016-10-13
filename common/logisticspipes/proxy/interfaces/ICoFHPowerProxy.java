package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;
import logisticspipes.recipes.CraftingParts;

public interface ICoFHPowerProxy {

	public boolean isEnergyReceiver(TileEntity tile);

	public ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile);

	public void addCraftingRecipes(CraftingParts parts);

	public ICoFHEnergyStorage getEnergyStorage(int i);

	public boolean isAvailable();
}
