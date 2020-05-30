package logisticspipes.logisticspipes;

import net.minecraft.util.EnumFacing;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifierStack;

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
		final ItemIdentifierStack itemidStack = item.getItemIdentifierStack();
		SinkReply reply = module.sinksItem(itemidStack.makeNormalStack(), itemidStack.getItem(), -1, 0, true, false, false);
		if (reply == null || reply.maxNumberOfItems < 0) {
			_chassiPipe.notifyOfItemArival(item.getInfo());
			return false;
		}

		if (reply.maxNumberOfItems > 0 && itemidStack.getStackSize() > reply.maxNumberOfItems) {
			EnumFacing o = _chassiPipe.getPointedOrientation();
			if (o == null) {
				o = EnumFacing.UP;
			}

			item.split(reply.maxNumberOfItems, o);
		}
		return true;
	}

}
