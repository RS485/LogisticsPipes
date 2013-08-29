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
		if (item.getItemStack() != null){
			_chassiPipe.recievedItem(item.getItemStack().stackSize);
		}
		return _chassiPipe.getPointedOrientation();
	}

	@Override
	public boolean stillWantItem(IRoutedItem item) {
		LogisticsModule module = _chassiPipe.getLogisticsModule();
		if (module == null) {
			_chassiPipe.notifyOfItemArival((RoutedEntityItem) item);
			return false;
		}
		if (!_chassiPipe.isEnabled())  {
			_chassiPipe.notifyOfItemArival((RoutedEntityItem) item);
			return false;
		}
		SinkReply reply = module.sinksItem(item.getIDStack().getItem(), -1, 0, true,false);
		if (reply == null) {
			_chassiPipe.notifyOfItemArival((RoutedEntityItem) item);
			return false;
		}
		
		if (reply.maxNumberOfItems != 0 && item.getItemStack().stackSize > reply.maxNumberOfItems){
			ForgeDirection o = _chassiPipe.getPointedOrientation();
			if (o==null || o == ForgeDirection.UNKNOWN) o = ForgeDirection.UP;
			
			item.split(reply.maxNumberOfItems, o);
		}
		return true;
	}

}
