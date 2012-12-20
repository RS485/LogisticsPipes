package logisticspipes.transport;

import logisticspipes.pipes.PipeItemsInvSysConnector;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import buildcraft.transport.EntityData;

public class TransportInvConnection extends PipeTransportLogistics {
	
	public TransportInvConnection() {}
	
	@Override
	public void scheduleRemoval(IPipedItem item) {
		super.scheduleRemoval(item);
		EntityData data = getEntityData(item);
		if(data == null) return;
		Position destPos = new Position(xCoord, yCoord, zCoord, data.output);

		destPos.moveForwards(1.0);

		TileEntity tile = worldObj.getBlockTileEntity((int) destPos.x, (int) destPos.y, (int) destPos.z);

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
