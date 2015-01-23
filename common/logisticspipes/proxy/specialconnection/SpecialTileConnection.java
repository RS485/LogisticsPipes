package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import net.minecraft.tileentity.TileEntity;

public class SpecialTileConnection {
	
	private List<ISpecialTileConnection> handler = new ArrayList<ISpecialTileConnection>();
	
	public void registerHandler(ISpecialTileConnection connectionHandler) {
		if(connectionHandler.init()) {
			handler.add(connectionHandler);
		}
	}
	
	public Collection<TileEntity> getConnectedPipes(TileEntity tile) {
		for(ISpecialTileConnection connectionHandler:handler) {
			if(connectionHandler.isType(tile)) {
				return connectionHandler.getConnections(tile);
			}
		}
		return new ArrayList<TileEntity>();
	}

	public boolean needsInformationTransition(TileEntity tile) {
		for(ISpecialTileConnection connectionHandler:handler) {
			if(connectionHandler.isType(tile)) {
				return connectionHandler.needsInformationTransition();
			}
		}
		return false;
	}

	public void transmit(TileEntity tile, IRoutedItem arrivingItem) {
		for(ISpecialTileConnection connectionHandler:handler) {
			if(connectionHandler.isType(tile)) {
				connectionHandler.transmit(tile, arrivingItem);
				break;
			}
		}
	}
}
