package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialPipedConnection;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;

public class SpecialPipeConnection {
	
	private List<ISpecialPipedConnection> handler = new ArrayList<ISpecialPipedConnection>();
	
	public void registerHandler(ISpecialPipedConnection connectionHandler) {
		if(connectionHandler.init()) {
			handler.add(connectionHandler);
		}
	}
	
	public List<IPipeInformationProvider> getConnectedPipes(IPipeInformationProvider startPipe) {
		for(ISpecialPipedConnection connectionHandler:handler) {
			if(connectionHandler.isType(startPipe)) {
				return connectionHandler.getConnections(startPipe);
			}
		}
		return new ArrayList<IPipeInformationProvider>();
	}
}
