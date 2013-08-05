package logisticspipes.transport;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.PipeRoutingConnectionType;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TravelingItem;

public class EntrencsTransport extends PipeTransportLogistics {
	
	public PipeItemsSystemEntranceLogistics pipe;
	
	@Override
	public ForgeDirection resolveDestination(TravelingItem data) {
		IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetOrCreateRoutedItem(data);
		if(routedItem.getDestination() < 0 || routedItem.getArrived()) {
			if(pipe.getLocalFreqUUID() != null) {
				if(pipe.useEnergy(5)) {
					for(ExitRoute router:pipe.getRouter().getIRoutersByCost()) {
						if(!router.containsFlag(PipeRoutingConnectionType.canRouteTo))
							continue;
						CoreRoutedPipe lPipe = router.destination.getPipe();
						if(lPipe instanceof PipeItemsSystemDestinationLogistics) {
							PipeItemsSystemDestinationLogistics dPipe = (PipeItemsSystemDestinationLogistics) lPipe;
							if(dPipe.getTargetUUID() != null) {
								if(dPipe.getTargetUUID().equals(pipe.getLocalFreqUUID())) {
									routedItem.setDestination(dPipe.getRouter().getSimpleID());
									routedItem.setArrived(false);
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
