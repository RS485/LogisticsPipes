package logisticspipes.transport;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.SearchNode;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.EntityData;

public class EntrencsTransport extends PipeTransportLogistics {
	
	public PipeItemsSystemEntranceLogistics pipe;
	
	@Override
	public ForgeDirection resolveDestination(EntityData data) {
		IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetOrCreateRoutedItem(container.worldObj, data);
		if(routedItem.getDestination() == null || routedItem.getArrived()) {
			if(pipe.getLocalFreqUUID() != null) {
				if(pipe.useEnergy(5)) {
					for(SearchNode router:pipe.getRouter().getIRoutersByCost()) {
						CoreRoutedPipe lPipe = router.node.getPipe();
						if(lPipe instanceof PipeItemsSystemDestinationLogistics) {
							PipeItemsSystemDestinationLogistics dPipe = (PipeItemsSystemDestinationLogistics) lPipe;
							if(dPipe.getTargetUUID() != null) {
								if(dPipe.getTargetUUID().equals(pipe.getLocalFreqUUID())) {
									routedItem.setDestination(dPipe.getRouter().getId());
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
