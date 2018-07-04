package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;
import logisticspipes.recipes.CraftingParts;

public interface ICoFHPowerProxy {

	boolean isEnergyReceiver(TileEntity tile, EnumFacing face);

	ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile, EnumFacing face);

	void addCraftingRecipes(CraftingParts parts);

	ICoFHEnergyStorage getEnergyStorage(int i);

	boolean isAvailable();
}
