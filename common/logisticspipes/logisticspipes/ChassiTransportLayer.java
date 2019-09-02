package logisticspipes.logisticspipes;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.SinkReply;

import net.minecraft.util.EnumFacing;

public class ChassiTransportLayer extends TransportLayer {

	private final PipeLogisticsChassi _chassiPipe;

	public ChassiTransportLayer(PipeLogisticsChassi chassiPipe) {
		_chassiPipe = chassiPipe;
	}

	@Override
	public EnumFacing itemArrived(IRoutedItem item, EnumFacing blocked) {
		if (item.getItemIdentifierStack() != null) {
			_chassiPipe.recievedItem(item.getItemIdentifierStack().getStackSize());
		}
		return _chassiPipe.getPointedOrientation();
	}

	@Override
	public boolean stillWantItem(IRoutedItem item) {
		LogisticsModule module = _chassiPipe.getLogisticsModule();
		if (module == null) {
			_chassiPipe.notifyOfItemArival(item.getInfo());
			return false;
		}
		if (!_chassiPipe.isEnabled()) {
			_chassiPipe.notifyOfItemArival(item.getInfo());
			return false;
		}
		SinkReply reply = module.sinksItem(item.getItemIdentifierStack().getItem(), -1, 0, true, false, false);
		if (reply == null || reply.maxNumberOfItems < 0) {
			_chassiPipe.notifyOfItemArival(item.getInfo());
			return false;
		}

		if (reply.maxNumberOfItems > 0 && item.getItemIdentifierStack().getStackSize() > reply.maxNumberOfItems) {
			EnumFacing o = _chassiPipe.getPointedOrientation();
			if (o == null) {
				o = EnumFacing.UP;
			}

			item.split(reply.maxNumberOfItems, o);
		}
		return true;
	}

}
