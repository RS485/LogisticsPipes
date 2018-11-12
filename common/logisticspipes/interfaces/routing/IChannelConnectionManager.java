package logisticspipes.interfaces.routing;

import java.util.List;
import java.util.UUID;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;

public interface IChannelConnectionManager {

	boolean hasChannelConnection(IRouter router);

	boolean addChannelConnection(UUID ident, IRouter router);

	List<CoreRoutedPipe> getConnectedPipes(IRouter router);

	void removeChannelConnection(IRouter router);
}
