package logisticspipes.interfaces.routing;

import java.util.Collection;

import net.minecraft.block.entity.BlockEntity;

import logisticspipes.logisticspipes.IRoutedItem;

public interface SpecialTileConnection {

	boolean init();

	boolean isType(BlockEntity tile);

	Collection<BlockEntity> getConnections(BlockEntity tile);

	boolean needsInformationTransition();

	void transmit(BlockEntity tile, IRoutedItem arrivingItem);
}
