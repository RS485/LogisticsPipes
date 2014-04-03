package logisticspipes.proxy.interfaces;

import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

public interface IThermalExpansionProxy {
	public boolean isTesseract(TileEntity tile);
	public List<TileEntity> getConnectedTesseracts(TileEntity tile);
	public boolean isItemConduit(TileEntity tile);
	public void handleLPInternalConduitChunkUnload(LogisticsTileGenericPipe pipe);
	public void handleLPInternalConduitRemove(LogisticsTileGenericPipe pipe);
	public void handleLPInternalConduitNeighborChange(LogisticsTileGenericPipe pipe);
	public void handleLPInternalConduitUpdate(LogisticsTileGenericPipe pipe);
	public boolean insertIntoConduit(TravelingItem arrivingItem, TileEntity tile, CoreRoutedPipe pipe);
	public boolean isSideFree(TileEntity tile, int side);
	public boolean isEnergyHandler(TileEntity tile);
	public int getMaxEnergyStored(TileEntity tile, ForgeDirection opposite);
	public int getEnergyStored(TileEntity tile, ForgeDirection opposite);
	public boolean canInterface(TileEntity tile, ForgeDirection opposite);
	public int receiveEnergy(TileEntity tile, ForgeDirection opposite, int i, boolean b);
	public boolean isTE();
}
