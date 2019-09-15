package logisticspipes.logisticspipes;

import net.minecraft.util.math.Direction;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.SinkReply;

public class ChassiTransportLayer extends TransportLayer {

	private final PipeLogisticsChassi _chassiPipe;

	public ChassiTransportLayer(PipeLogisticsChassi chassiPipe) {
		_chassiPipe = chassiPipe;
	}

	@Override
	public Direction itemArrived(IRoutedItem item, Direction blocked) {
		if (item.getItemStack() != null) {
			_chassiPipe.recievedItem(item.getItemStack().getStackSize());
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
		SinkReply reply = module.sinksItem(item.getItemStack().getItem(), -1, 0, true, false, false);
		if (reply == null || reply.maxNumberOfItems < 0) {
			_chassiPipe.notifyOfItemArival(item.getInfo());
			return false;
		}

		if (reply.maxNumberOfItems > 0 && item.getItemStack().getStackSize() > reply.maxNumberOfItems) {
			Direction o = _chassiPipe.getPointedOrientation();
			if (o == null) {
				o = Direction.UP;
			}

			item.split(reply.maxNumberOfItems, o);
		}
		return true;
	}

}
