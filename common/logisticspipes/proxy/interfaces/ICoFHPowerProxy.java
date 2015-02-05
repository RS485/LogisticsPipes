package logisticspipes.proxy.interfaces;

import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import net.minecraft.tileentity.TileEntity;

public interface ICoFHPowerProxy {
	public boolean isEnergyReceiver(TileEntity tile);
	public ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile);
	public boolean isAvailable();
	public void addCraftingRecipes(ICraftingParts parts);
}
