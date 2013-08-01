package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import net.minecraft.tileentity.TileEntity;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.TileGenericPipe;

public class SpecialTileConnection {
	
	private List<ISpecialTileConnection> handler = new ArrayList<ISpecialTileConnection>();
	
	public void registerHandler(ISpecialTileConnection connectionHandler) {
		if(connectionHandler.init()) {
			handler.add(connectionHandler);
		}
	}
	
	public List<TileGenericPipe> getConnectedPipes(TileEntity tile) {
		for(ISpecialTileConnection connectionHandler:handler) {
			if(connectionHandler.isType(tile)) {
				return connectionHandler.getConnections(tile);
			}
		}
		return new ArrayList<TileGenericPipe>();
	}

	public boolean needsInformationTransition(TileEntity tile) {
		for(ISpecialTileConnection connectionHandler:handler) {
			if(connectionHandler.isType(tile)) {
				return connectionHandler.needsInformationTransition();
			}
		}
		return false;
	}

	public void transmit(TileEntity tile, TravelingItem data) {
		for(ISpecialTileConnection connectionHandler:handler) {
			if(connectionHandler.isType(tile)) {
				connectionHandler.transmit(tile, data);
			}
		}
	}
}
