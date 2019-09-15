package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.entity.BlockEntity;

import logisticspipes.interfaces.routing.SpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;

public class SpecialTileConnectionRegistry {

	public static final SpecialTileConnectionRegistry INSTANCE = new SpecialTileConnectionRegistry();

	private List<SpecialTileConnection> handler = new ArrayList<>();

	private SpecialTileConnectionRegistry() {}

	public void registerHandler(SpecialTileConnection connectionHandler) {
		if (connectionHandler.init()) {
			handler.add(connectionHandler);
		}
	}

	public Collection<BlockEntity> getConnectedPipes(BlockEntity tile) {
		for (SpecialTileConnection connectionHandler : handler) {
			if (connectionHandler.isType(tile)) {
				return connectionHandler.getConnections(tile);
			}
		}
		return new ArrayList<>();
	}

	public boolean needsInformationTransition(BlockEntity tile) {
		for (SpecialTileConnection connectionHandler : handler) {
			if (connectionHandler.isType(tile)) {
				return connectionHandler.needsInformationTransition();
			}
		}
		return false;
	}

	public void transmit(BlockEntity tile, IRoutedItem arrivingItem) {
		for (SpecialTileConnection connectionHandler : handler) {
			if (connectionHandler.isType(tile)) {
				connectionHandler.transmit(tile, arrivingItem);
				break;
			}
		}
	}

	public boolean isType(BlockEntity tile) {
		for (SpecialTileConnection connectionHandler : handler) {
			if (connectionHandler.isType(tile)) {
				return true;
			}
		}
		return false;
	}

}
