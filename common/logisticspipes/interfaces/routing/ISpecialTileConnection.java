package logisticspipes.interfaces.routing;

import java.util.Collection;

import logisticspipes.logisticspipes.IRoutedItem;

import net.minecraft.tileentity.TileEntity;

public interface ISpecialTileConnection {

	boolean init();

	boolean isType(TileEntity tile);

	Collection<TileEntity> getConnections(TileEntity tile);

	boolean needsInformationTransition();

	void transmit(TileEntity tile, IRoutedItem arrivingItem);
}
