package logisticspipes.logisticspipes;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.SinkReply;
import buildcraft.api.core.Orientations;

public class ChassiTransportLayer extends TransportLayer{

	private final PipeLogisticsChassi _chassiPipe;
	
	public ChassiTransportLayer(PipeLogisticsChassi chassiPipe) {
		_chassiPipe = chassiPipe;
	}

	@Override
	public Orientations itemArrived(IRoutedItem item) {
//		item.setSpeedBoost(50F);	//Boost speed to help item arrive faster so we don't get overflow
		return _chassiPipe.getPointedOrientation();
	}

	@Override
	public boolean stillWantItem(IRoutedItem item) {
		ILogisticsModule module = _chassiPipe.getLogisticsModule();
		if (module == null) return false;
		if (!_chassiPipe.isEnabled()) return false;
		SinkReply reply = module.sinksItem(item.getItemStack());
		if (reply == null) return false;
		
		if (reply.maxNumberOfItems != 0 && item.getItemStack().stackSize > reply.maxNumberOfItems){
			Orientations o = _chassiPipe.getPointedOrientation();
			if (o==null || o == Orientations.Unknown) o = Orientations.YPos;
			
			IRoutedItem newItem = item.split(_chassiPipe.worldObj, reply.maxNumberOfItems, o.reverse());
			return false;
		}
		
		return module.sinksItem(item.getItemStack()) != null;	
	}

}
