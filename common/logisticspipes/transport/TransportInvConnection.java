package logisticspipes.transport;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class TransportInvConnection extends PipeTransportLogistics {

	public TransportInvConnection() {
		super(true);
	}

	@Override
	protected boolean isItemExitable(ItemIdentifierStack stack) {
		return true;
	}

	@Override
	protected void insertedItemStack(ItemRoutingInformation info, TileEntity tile) {
		if (tile != null && tile.hasCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, CoordinateUtils.getDirectionFromTo(new DoubleCoordinates(getPipe().container.getPos()), new DoubleCoordinates(tile)).getOpposite())) {
			((PipeItemsInvSysConnector) container.pipe).handleItemEnterInv(info, tile);
		}
	}
}
