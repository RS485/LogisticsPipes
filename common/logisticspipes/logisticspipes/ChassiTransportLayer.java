package logisticspipes.logisticspipes;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.utils.SinkReply;
import net.minecraftforge.common.ForgeDirection;

public class ChassiTransportLayer extends TransportLayer{

	private final PipeLogisticsChassi _chassiPipe;
	
	public ChassiTransportLayer(PipeLogisticsChassi chassiPipe) {
		_chassiPipe = chassiPipe;
	}

	@Override
	public ForgeDirection itemArrived(IRoutedItem item, ForgeDirection blocked) {
		item.setArrived(true);
		this._chassiPipe.getRouter().inboundItemArrived((RoutedEntityItem) item); //NOT TESTED
		return _chassiPipe.getPointedOrientation();
	}

	@Override
	public boolean stillWantItem(IRoutedItem item) {
		LogisticsModule module = _chassiPipe.getLogisticsModule();
		if (module == null) return false;
		if (!_chassiPipe.isEnabled()) return false;
		SinkReply reply = module.sinksItem(item.getIDStack().getItem(), -1, 0, true,false);
		if (reply == null) return false;
		
		if (reply.maxNumberOfItems != 0 && item.getItemStack().stackSize > reply.maxNumberOfItems){
			ForgeDirection o = _chassiPipe.getPointedOrientation();
			if (o==null || o == ForgeDirection.UNKNOWN) o = ForgeDirection.UP;
			
			item.split(reply.maxNumberOfItems, o);
		}
		return true;
	}

}
