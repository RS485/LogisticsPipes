package logisticspipes.proxy.buildcraft;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.routing.ItemRoutingInformation;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.transport.TravelingItem;

public class LPRoutedBCTravelingItem extends TravelingItem {

	private static InsertionHandler LP_INSERTIONHANDLER = new InsertionHandler() {
		@Override
		public boolean canInsertItem(TravelingItem item, IInventory inv) {
			if(item.getItemStack() != null && item.getItemStack().getItem() instanceof IItemAdvancedExistance && !((IItemAdvancedExistance)item.getItemStack().getItem()).canExistInNormalInventory(item.getItemStack())) return false;
			return true;
		}
	};
	
	public LPRoutedBCTravelingItem() {
		this.setInsertionHandler(LP_INSERTIONHANDLER);
	}
	
	@Getter
	@Setter
	private ItemRoutingInformation routingInformation;
	
	public void saveToExtraNBTData() {
		if(routingInformation == null) return;
		NBTTagCompound nbt = this.getExtraData();
		NBTTagCompound info = new NBTTagCompound();
		routingInformation.writeToNBT(info);
		nbt.setTag("LPRoutingInformation", info);
	}
	
	public static ItemRoutingInformation restoreFromExtraNBTData(TravelingItem item) {
		if(!item.hasExtraData()) return null;
		NBTTagCompound nbt = item.getExtraData();
		if(nbt.hasKey("LPRoutingInformation")) {
			ItemRoutingInformation routingInformation = new ItemRoutingInformation();
			routingInformation.readFromNBT(nbt.getCompoundTag("LPRoutingInformation"));
			return routingInformation;
		}
		return null;
	}
}
