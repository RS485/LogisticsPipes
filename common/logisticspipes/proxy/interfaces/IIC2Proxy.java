package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.recipes.CraftingParts;

public interface IIC2Proxy {

	void addCraftingRecipes(CraftingParts parts);

	boolean hasIC2();

	void registerToEneryNet(TileEntity tile);

	void unregisterToEneryNet(TileEntity tile);

	boolean acceptsEnergyFrom(TileEntity energy, TileEntity tile, EnumFacing opposite);

	boolean isEnergySink(TileEntity tile);

	double demandedEnergyUnits(TileEntity tile);

	double injectEnergyUnits(TileEntity tile, EnumFacing opposite, double d);

}
