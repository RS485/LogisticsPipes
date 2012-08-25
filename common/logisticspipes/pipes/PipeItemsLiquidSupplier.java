package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.main.RoutedPipe;
import logisticspipes.main.SimpleServiceLocator;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class PipeItemsLiquidSupplier extends RoutedPipe implements IRequestItems, IItemTravelingHook{

	
	public PipeItemsLiquidSupplier(int itemID) {
		super(new LogicLiquidSupplier(), itemID);
		((PipeTransportItems) transport).travelHook = this;
	}

	@Override
	public int getCenterTexture() {
		return LogisticsPipes.LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE;
	} 

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}
	
	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		if (!(tile instanceof ILiquidTank)) return;
		if (tile instanceof TileGenericPipe) return;
		ILiquidTank container = (ILiquidTank) tile;
		//container.getLiquidSlots()[0].getLiquidQty();
		if (data.item == null) return;
		if (data.item.getItemStack() == null) return;
		LiquidStack liquidId = LiquidManager.getLiquidForFilledItem(data.item.getItemStack());
		if (liquidId == null) return;
		while (data.item.getItemStack().stackSize > 0 && container.fill(liquidId, false) == liquidId.amount){
			container.fill(liquidId, true);
			data.item.getItemStack().stackSize--;
			
			if (data.item.getItemStack().itemID >= 0 && data.item.getItemStack().itemID < Item.itemsList.length){
				Item item = Item.itemsList[data.item.getItemStack().itemID];
				if (item.hasContainerItem()){
					Item containerItem = item.getContainerItem();
					IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(new ItemStack(containerItem, 1), this.worldObj);
					itemToSend.setSource(this.getRouter().getId());
					this.queueRoutedItem(itemToSend, data.orientation);
				}
			}
		}
		if (data.item.getItemStack().stackSize < 1){
			((PipeTransportItems)this.transport).scheduleRemoval(data.item);
		}
	}

	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {}
	
	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
}
