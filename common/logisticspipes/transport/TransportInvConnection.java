package logisticspipes.transport;

import logisticspipes.pipes.PipeItemsInvSysConnector;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.transport.IPipedItem;
import buildcraft.transport.EntityData;

public class TransportInvConnection extends PipeTransportLogistics {
	
	public TransportInvConnection() {}
	
	@Override
	protected void insertedItemStack(EntityData data, TileEntity tile) {
		if(tile instanceof IInventory) {
			((PipeItemsInvSysConnector)this.container.pipe).handleItemEnterInv(data,tile);
		}
	}
	
	private EntityData getEntityData(IPipedItem item) {
		for(EntityData data:travelingEntities.values()) {
			if(data.item == item) {
				return data;
			}
		}
		return null;
	}
}
