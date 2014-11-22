package logisticspipes.interfaces.routing;

import logisticspipes.logisticspipes.IRoutedItem;
import net.minecraft.tileentity.TileEntity;

import java.util.Collection;

public interface ISpecialTileConnection {
	public boolean init();
	public boolean isType(TileEntity tile);
	public Collection<TileEntity> getConnections(TileEntity tile);
	public boolean needsInformationTransition();
	public void transmit(TileEntity tile, IRoutedItem arrivingItem);
}
