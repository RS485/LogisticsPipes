package logisticspipes.proxy.buildcraft;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.routing.ItemRoutingInformation;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.transport.TravelingItem;
import lombok.Getter;
import lombok.Setter;

public class LPRoutedBCTravelingItem extends TravelingItem {

	private static InsertionHandler LP_INSERTIONHANDLER = new InsertionHandler() {

		@Override
		public boolean canInsertItem(TravelingItem item, IInventory inv) {
			if (item.getItemStack() != null && item.getItemStack().getItem() instanceof IItemAdvancedExistance && !((IItemAdvancedExistance) item.getItemStack().getItem()).canExistInNormalInventory(item.getItemStack())) {
				return false;
			}
			return true;
		}
	};

	public LPRoutedBCTravelingItem() {
		super(TravelingItem.make().id);
		TravelingItem.getCache().cache(this);
		setInsertionHandler(LPRoutedBCTravelingItem.LP_INSERTIONHANDLER);
	}

	private SecurityManager hackToGetCaller = new SecurityManager() {

		@Override
		public Object getSecurityContext() {
			return getClassContext();
		}
	};

	@Override
	public ItemStack getItemStack() {
		Class<?>[] caller = (Class<?>[]) hackToGetCaller.getSecurityContext();
		if (caller[2].getName().equals("buildcraft.transport.network.PacketPipeTransportItemStackRequest")) {
			ItemStack stack = super.getItemStack();
			if (stack == null) {
				return stack;
			}
			stack = stack.copy();
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setString("LogsitcsPipes_ITEM_ON_TRANSPORTATION", "YES");
			return stack;
		} else {
			return super.getItemStack();
		}
	}

	@Getter
	@Setter
	private ItemRoutingInformation routingInformation;

	public void saveToExtraNBTData() {
		if (routingInformation == null) {
			return;
		}
		NBTTagCompound nbt = getExtraData();
		NBTTagCompound info = new NBTTagCompound();
		routingInformation.writeToNBT(info);
		nbt.setTag("LPRoutingInformation", info);
	}

	public static ItemRoutingInformation restoreFromExtraNBTData(TravelingItem item) {
		if (!item.hasExtraData()) {
			return null;
		}
		NBTTagCompound nbt = item.getExtraData();
		if (nbt.hasKey("LPRoutingInformation")) {
			ItemRoutingInformation routingInformation = new ItemRoutingInformation();
			routingInformation.readFromNBT(nbt.getCompoundTag("LPRoutingInformation"));
			return routingInformation;
		}
		return null;
	}

	@Override
	public boolean ignoreWeight() {
		return true;
	}
}
