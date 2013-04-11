package logisticspipes.interfaces.routing;

import java.util.List;

import buildcraft.transport.TileGenericPipe;

public interface ISpecialPipedConnection {
	public boolean init();
	public boolean isType(TileGenericPipe tile);
	public List<TileGenericPipe> getConnections(TileGenericPipe tile);
}
