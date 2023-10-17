package logisticspipes.transport;

import java.util.Objects;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.item.ItemIdentifierStack;

public class TransportInvConnection extends PipeTransportLogistics {

	public TransportInvConnection() {
		super(true);
	}

	@Override
	protected boolean isItemUnwanted(ItemIdentifierStack stack) {
		return false;
	}

	@Override
	protected void inventorySystemConnectorHook(ItemRoutingInformation info, TileEntity tile) {
		if (tile == null) {
			return;
		}

		final EnumFacing orientationOfTilewithTile = OrientationsUtil.getOrientationOfTilewithTile(getPipe().container, tile);
		Objects.requireNonNull(orientationOfTilewithTile, "Could not get direction from pipe and tile entity");

		if (tile.hasCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, orientationOfTilewithTile.getOpposite())) {
			((PipeItemsInvSysConnector) container.pipe).handleItemEnterInv(info, tile);
		}
	}
}
