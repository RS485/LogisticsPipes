package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.transport.LPTravelingItem;

import net.minecraft.tileentity.TileEntity;
import buildcraft.transport.TravelingItem;

public interface ISpecialTileConnection {
	public boolean init();
	public boolean isType(TileEntity tile);
	public List<TileEntity> getConnections(TileEntity tile);
	public boolean needsInformationTransition();
	public void transmit(TileEntity tile, IRoutedItem arrivingItem);
}
