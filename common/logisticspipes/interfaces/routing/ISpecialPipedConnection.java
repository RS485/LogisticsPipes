package logisticspipes.interfaces.routing;

import java.util.EnumSet;
import java.util.List;

import logisticspipes.proxy.specialconnection.SpecialPipeConnection.ConnectionInformation;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;

import net.minecraft.util.EnumFacing;

public interface ISpecialPipedConnection {

	public boolean init();

	public boolean isType(IPipeInformationProvider startPipe);

	public List<ConnectionInformation> getConnections(IPipeInformationProvider startPipe, EnumSet<PipeRoutingConnectionType> connection, EnumFacing side);
}
