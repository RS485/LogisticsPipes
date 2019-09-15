package logisticspipes.interfaces.routing;

import java.util.List;
import java.util.UUID;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.Router;

public interface ChannelConnectionManager {

	boolean hasChannelConnection(Router router);

	boolean addChannelConnection(UUID ident, Router router);

	List<CoreRoutedPipe> getConnectedPipes(Router router);

	void removeChannelConnection(Router router);
}
