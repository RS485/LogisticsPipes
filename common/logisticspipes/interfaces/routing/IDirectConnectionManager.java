package logisticspipes.interfaces.routing;

import logisticspipes.routing.IRouter;

public interface IDirectConnectionManager {
	public boolean hasDirectConnection(IRouter router);
	public boolean addDirectConnection(String ident, IRouter router);
}
