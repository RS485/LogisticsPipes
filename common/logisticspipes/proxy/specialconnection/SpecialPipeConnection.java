package logisticspipes.proxy.specialconnection;

import logisticspipes.interfaces.routing.ISpecialPipedConnection;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SpecialPipeConnection {

	private List<ISpecialPipedConnection> handler = new ArrayList<ISpecialPipedConnection>();

	public void registerHandler(ISpecialPipedConnection connectionHandler) {
		if (connectionHandler.init()) {
			handler.add(connectionHandler);
		}
	}

	public List<ConnectionInformation> getConnectedPipes(IPipeInformationProvider startPipe, EnumSet<PipeRoutingConnectionType> connection, EnumFacing side) {
		for (ISpecialPipedConnection connectionHandler : handler) {
			if (connectionHandler.isType(startPipe)) {
				return connectionHandler.getConnections(startPipe, connection, side);
			}
		}
		return new ArrayList<ConnectionInformation>();
	}

	@Data
	@AllArgsConstructor
	public static class ConnectionInformation {

		private IPipeInformationProvider connectedPipe;
		private EnumSet<PipeRoutingConnectionType> connectionFlags;
		private EnumFacing insertOrientation;
		private EnumFacing exitOrientation;
		private double distance;
	}
}
