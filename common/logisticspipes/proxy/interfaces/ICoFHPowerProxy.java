package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;
import logisticspipes.recipes.CraftingParts;

public interface ICoFHPowerProxy {

	boolean isEnergyReceiver(TileEntity tile);

	ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile);

	void addCraftingRecipes(CraftingParts parts);

	ICoFHEnergyStorage getEnergyStorage(int i);

	boolean isAvailable();
}
