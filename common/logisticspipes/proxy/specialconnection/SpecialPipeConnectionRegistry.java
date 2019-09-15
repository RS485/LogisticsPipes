package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.util.math.Direction;

import lombok.AllArgsConstructor;
import lombok.Data;

import logisticspipes.interfaces.routing.SpecialPipeConnection;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;

public class SpecialPipeConnectionRegistry {

	public static final SpecialPipeConnectionRegistry INSTANCE = new SpecialPipeConnectionRegistry();

	private List<SpecialPipeConnection> handler = new ArrayList<>();

	private SpecialPipeConnectionRegistry() {}

	public void registerHandler(SpecialPipeConnection connectionHandler) {
		if (connectionHandler.init()) {
			handler.add(connectionHandler);
		}
	}

	public List<ConnectionInformation> getConnectedPipes(IPipeInformationProvider startPipe, EnumSet<PipeRoutingConnectionType> connection, Direction side) {
		for (SpecialPipeConnection connectionHandler : handler) {
			if (connectionHandler.isType(startPipe)) {
				return connectionHandler.getConnections(startPipe, connection, side);
			}
		}
		return new ArrayList<>();
	}

	@Data
	@AllArgsConstructor
	public static class ConnectionInformation {

		private IPipeInformationProvider connectedPipe;
		private EnumSet<PipeRoutingConnectionType> connectionFlags;
		private Direction insertOrientation;
		private Direction exitOrientation;
		private double distance;

	}

}
