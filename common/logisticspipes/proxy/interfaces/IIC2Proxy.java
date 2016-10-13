package logisticspipes.proxy.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import logisticspipes.recipes.CraftingParts;

public interface IIC2Proxy {

	boolean isElectricItem(ItemStack stack);

	boolean isSimilarElectricItem(ItemStack stack, ItemStack template);

	boolean isFullyCharged(ItemStack stack);

	boolean isFullyDischarged(ItemStack stack);

	boolean isPartiallyCharged(ItemStack stack);

	void addCraftingRecipes(CraftingParts parts);

	boolean hasIC2();

	void registerToEneryNet(TileEntity tile);

	void unregisterToEneryNet(TileEntity tile);

	boolean acceptsEnergyFrom(TileEntity energy, TileEntity tile, ForgeDirection opposite);

	boolean isEnergySink(TileEntity tile);

	double demandedEnergyUnits(TileEntity tile);

	double injectEnergyUnits(TileEntity tile, ForgeDirection opposite, double d);

}
