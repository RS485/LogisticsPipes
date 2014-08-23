package logisticspipes.proxy.interfaces;

import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IThermalExpansionProxy {
	public boolean isTesseract(TileEntity tile);
	public List<TileEntity> getConnectedTesseracts(TileEntity tile);
	public boolean isEnergyHandler(TileEntity tile);
	public int getMaxEnergyStored(TileEntity tile, ForgeDirection opposite);
	public int getEnergyStored(TileEntity tile, ForgeDirection opposite);
	public boolean canConnectEnergy(TileEntity tile, ForgeDirection opposite);
	public int receiveEnergy(TileEntity tile, ForgeDirection opposite, int i, boolean b);
	public boolean isTE();
	public void addCraftingRecipes(ICraftingParts parts);
}
