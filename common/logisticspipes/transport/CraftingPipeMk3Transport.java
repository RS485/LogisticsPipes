package logisticspipes.transport;

import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.EntityData;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class CraftingPipeMk3Transport extends CraftingPipeTransport {
	
	public PipeItemsCraftingLogisticsMk3 pipe;
	
	public CraftingPipeMk3Transport() {
		super();
		travelHook = new LogisticsItemTravelingHook(worldObj, xCoord, yCoord, zCoord, this) {
			@Override
			public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
				scheduleRemoval(data.item);
				handleTileReached(data, tile);
			}
		};
	}
	
	private void handleTileReached(EntityData data, TileEntity tile) {
		if (tile instanceof IPipeEntry)
			((IPipeEntry) tile).entityEntering(data.item, data.output);
		else if (tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			TileGenericPipe pipe = (TileGenericPipe) tile;

			((PipeTransportItems) pipe.pipe.transport).entityEntering(data.item, data.output);
		} else if (tile instanceof IInventory) {
			ItemStack added = Transactor.getTransactorFor(tile).add(data.item.getItemStack(), data.input.reverse(), true);

			if (!CoreProxy.proxy.isRenderWorld(worldObj))
				if(added.stackSize >= data.item.getItemStack().stackSize)
					data.item.remove();
				else {
					data.item.getItemStack().stackSize -= added.stackSize;
					
					data.item.getItemStack().stackSize = pipe.inv.addCompressed(data.item.getItemStack());
					
					if(data.item.getItemStack().stackSize > 0) {
						EntityItem dropped = data.item.toEntityItem(data.output);
						if (dropped != null)
							// On SMP, the client side doesn't actually drops
							// items
							onDropped(dropped);	
					}
				}
		} else {
			if (travelHook != null)
				travelHook.drop(this, data);

			EntityItem dropped = data.item.toEntityItem(data.output);

			if (dropped != null)
				// On SMP, the client side doesn't actually drops
				// items
				onDropped(dropped);
		}
	}
}
