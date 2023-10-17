package logisticspipes.transport;

import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;

public class EntrencsTransport extends PipeTransportLogistics {

	public EntrencsTransport() {
		super(true);
	}

	public PipeItemsSystemEntranceLogistics pipe;

	@Override
	public RoutingResult resolveDestination(LPTravelingItemServer data) {
		if (data.getDestination() < 0 || data.getArrived()) {
			if (pipe.getLocalFreqUUID() != null) {
				if (pipe.useEnergy(5)) {
					for (ExitRoute router : pipe.getRouter().getIRoutersByCost()) {
						if (!router.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
							continue;
						}
						CoreRoutedPipe lPipe = router.destination.getPipe();
						if (lPipe instanceof PipeItemsSystemDestinationLogistics) {
							PipeItemsSystemDestinationLogistics dPipe = (PipeItemsSystemDestinationLogistics) lPipe;
							if (dPipe.getTargetUUID() != null) {
								if (dPipe.getTargetUUID().equals(pipe.getLocalFreqUUID())) {
									data.setDestination(dPipe.getRouter().getSimpleID());
									data.setArrived(false);
								}
							}
						}
					}
				}
			}
		}
		return super.resolveDestination(data);
	}
}
