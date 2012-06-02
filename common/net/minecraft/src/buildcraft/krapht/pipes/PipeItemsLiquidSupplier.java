package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logic.LogicBuilderSupplier;
import net.minecraft.src.buildcraft.krapht.logic.LogicLiquidSupplier;
import net.minecraft.src.buildcraft.krapht.logic.TemporaryLogic;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.transport.EntityData;
import net.minecraft.src.buildcraft.transport.IItemTravelingHook;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.ForgeHooks;
import net.minecraft.src.forge.ForgeHooksClient;

public class PipeItemsLiquidSupplier extends RoutedPipe implements IRequestItems, IItemTravelingHook{

	
	public PipeItemsLiquidSupplier(int itemID) {
		super(new LogicLiquidSupplier(), itemID);
		((PipeTransportItems) transport).travelHook = this;
	}

	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE;
	} 

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}
	
	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		if (!(tile instanceof ILiquidContainer)) return;
		if (tile instanceof TileGenericPipe) return;
		ILiquidContainer container = (ILiquidContainer) tile;
		container.getLiquidSlots()[0].getLiquidQty();
		if (data.item == null) return;
		if (data.item.item == null) return;
		int liquidId = BuildCraftAPI.getLiquidForFilledItem(data.item.item);
		if (liquidId == 0) return;
		while (data.item.item.stackSize > 0 && container.fill(data.orientation.reverse(), BuildCraftAPI.BUCKET_VOLUME, liquidId, false) == BuildCraftAPI.BUCKET_VOLUME){
			container.fill(data.orientation.reverse(), BuildCraftAPI.BUCKET_VOLUME, liquidId, true);
			data.item.item.stackSize--;
			
			if (data.item.item.itemID >= 0 && data.item.item.itemID < Item.itemsList.length){
				Item item = Item.itemsList[data.item.item.itemID];
				if (item.hasContainerItem()){
					Item containerItem = item.getContainerItem();
					IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(new ItemStack(containerItem, 1), this.worldObj);
					itemToSend.setSource(this.getRouter().getId());
					this.queueRoutedItem(itemToSend, data.orientation);
				}
			}
		}
		if (data.item.item.stackSize < 1){
			((PipeTransportItems)this.transport).scheduleRemoval(data.item);
		}
	}

	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {}
	

}
