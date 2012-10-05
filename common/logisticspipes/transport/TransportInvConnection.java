package logisticspipes.transport;

import logisticspipes.pipes.PipeItemsInvSysConnector;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.EntityData;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class TransportInvConnection extends PipeTransportLogistics {
	
	public TransportInvConnection() {
		travelHook = new LogisticsItemTravelingHook(worldObj, xCoord, yCoord, zCoord, this) {
			@Override
			public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
				scheduleRemoval(data.item);
				handleTileReached(data, tile);
			}
		};
	}
	
	@Override
	public void scheduleRemoval(IPipedItem item) {
		super.scheduleRemoval(item);
		EntityData data = getEntityData(item);
		if(data == null) return;
		Position destPos = new Position(xCoord, yCoord, zCoord, data.orientation);

		destPos.moveForwards(1.0);

		TileEntity tile = worldObj.getBlockTileEntity((int) destPos.x, (int) destPos.y, (int) destPos.z);

		if(tile instanceof IInventory) {
			((PipeItemsInvSysConnector)this.container.pipe).handleItemEnterInv(data,tile);
		}
	}
	
	private void handleTileReached(EntityData data, TileEntity tile) {
		if (tile instanceof IPipeEntry)
			((IPipeEntry) tile).entityEntering(data.item, data.orientation);
		else if (tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			TileGenericPipe pipe = (TileGenericPipe) tile;

			((PipeTransportItems) pipe.pipe.transport).entityEntering(data.item, data.orientation);
		} else if (tile instanceof IInventory) {
			ItemStack added = Transactor.getTransactorFor(tile).add(data.item.getItemStack(), data.orientation.reverse(), true);

			if (!CoreProxy.proxy.isRenderWorld(worldObj))
				if(added.stackSize >= data.item.getItemStack().stackSize) {
					//data.item.remove();
					if (PipeManager.getAllEntities().containsKey(data.item.getEntityId())) {
						PipeManager.getAllEntities().remove(data.item.getEntityId());
					}
				} else {
					data.item.getItemStack().stackSize -= added.stackSize;
					EntityItem dropped = data.item.toEntityItem(data.orientation);

					if (dropped != null)
						// On SMP, the client side doesn't actually drops
						// items
						onDropped(dropped);
				}
		} else {
			if (travelHook != null)
				travelHook.drop(this, data);

			EntityItem dropped = data.item.toEntityItem(data.orientation);

			if (dropped != null)
				// On SMP, the client side doesn't actually drops
				// items
				onDropped(dropped);
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
