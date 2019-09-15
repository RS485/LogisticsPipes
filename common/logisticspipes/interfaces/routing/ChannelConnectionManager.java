package logisticspipes.interfaces.routing;

import java.util.List;
import java.util.UUID;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.Router;
import logisticspipes.routing.RouterManagerImpl;

public interface ChannelConnectionManager {

	static ChannelConnectionManager getInstance() {
		return RouterManagerImpl.INSTANCE;
	}

	boolean hasChannelConnection(Router router);

	boolean addChannelConnection(UUID ident, Router router);

	List<CoreRoutedPipe> getConnectedPipes(Router router);

	void removeChannelConnection(Router router);

}
