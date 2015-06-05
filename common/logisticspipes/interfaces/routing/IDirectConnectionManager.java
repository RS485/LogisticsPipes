package logisticspipes.interfaces.routing;

import java.util.UUID;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;

public interface IDirectConnectionManager {

	public boolean hasDirectConnection(IRouter router);

	public boolean addDirectConnection(UUID ident, IRouter router);

	public CoreRoutedPipe getConnectedPipe(IRouter router);

	public void removeDirectConnection(IRouter router);
}
