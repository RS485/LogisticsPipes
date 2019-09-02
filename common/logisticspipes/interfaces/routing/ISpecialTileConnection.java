package logisticspipes.interfaces.routing;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.logisticspipes.IRoutedItem;

public interface ISpecialTileConnection {

	boolean init();

	boolean isType(TileEntity tile);

	Collection<TileEntity> getConnections(TileEntity tile);

	boolean needsInformationTransition();

	void transmit(TileEntity tile, IRoutedItem arrivingItem);
}
