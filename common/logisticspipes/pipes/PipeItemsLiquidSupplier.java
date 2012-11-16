package logisticspipes.pipes;

import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.transport.PipeTransportLogistics;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class PipeItemsLiquidSupplier extends RoutedPipe implements IRequestItems, IItemTravelingHook{

	
	public PipeItemsLiquidSupplier(int itemID) {
		super(new PipeTransportLogistics() {

			@Override
			public boolean isPipeConnected(TileEntity tile) {
				if(super.isPipeConnected(tile)) return true;
				if(tile instanceof TileGenericPipe) return false;
				if (tile instanceof ITankContainer) {
					ITankContainer liq = (ITankContainer) tile;
					if (liq.getTanks(ForgeDirection.UNKNOWN) != null && liq.getTanks(ForgeDirection.UNKNOWN).length > 0)
						return true;
				}
				return false;
			}
		}, new LogicLiquidSupplier(), itemID);
		((PipeTransportItems) transport).travelHook = this;
		((LogicLiquidSupplier) logic)._power = this;
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE;
	} 

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}
	
	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		if (!(tile instanceof ITankContainer)) return;
		if (tile instanceof TileGenericPipe) return;
		ITankContainer container = (ITankContainer) tile;
		//container.getLiquidSlots()[0].getLiquidQty();
		if (data.item == null) return;
		if (data.item.getItemStack() == null) return;
		LiquidStack liquidId = LiquidContainerRegistry.getLiquidForFilledItem(data.item.getItemStack());
		if (liquidId == null) return;
		while (data.item.getItemStack().stackSize > 0 && container.fill(data.output, liquidId, false) == liquidId.amount && this.useEnergy(5)) {
			container.fill(data.output, liquidId, true);
			data.item.getItemStack().stackSize--;
			
			if (data.item.getItemStack().itemID >= 0 && data.item.getItemStack().itemID < Item.itemsList.length){
				Item item = Item.itemsList[data.item.getItemStack().itemID];
				if (item.hasContainerItem()){
					Item containerItem = item.getContainerItem();
					IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(new ItemStack(containerItem, 1), this.worldObj);
					itemToSend.setSource(this.getRouter().getId());
					this.queueRoutedItem(itemToSend, data.output);
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
