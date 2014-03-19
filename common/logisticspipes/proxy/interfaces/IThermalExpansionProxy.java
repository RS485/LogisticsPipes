package logisticspipes.proxy.interfaces;

import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.tileentity.TileEntity;
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
	public boolean isTE();
}
