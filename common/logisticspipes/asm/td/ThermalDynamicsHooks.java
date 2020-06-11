package logisticspipes.asm.td;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cofh.thermaldynamics.duct.item.TravelingItem;

import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.routing.ItemRoutingInformation;

public class ThermalDynamicsHooks {

	public static void travelingItemToNBT(TravelingItem travelingItem, NBTTagCompound paramNBTTagCompound) {
		if (((ILPTravelingItemInfo) travelingItem).getLPRoutingInfoAddition() != null) {
			NBTTagCompound save = new NBTTagCompound();
			((ItemRoutingInformation) ((ILPTravelingItemInfo) travelingItem).getLPRoutingInfoAddition()).writeToNBT(save);
			paramNBTTagCompound.setTag("LPRoutingInformation", save);
		}
	}

	public static void travelingItemNBTContructor(TravelingItem travelingItem, NBTTagCompound paramNBTTagCompound) {
		if (!paramNBTTagCompound.hasKey("LPRoutingInformation")) {
			return;
		}
		((ILPTravelingItemInfo) travelingItem).setLPRoutingInfoAddition(new ItemRoutingInformation());
		((ItemRoutingInformation) ((ILPTravelingItemInfo) travelingItem).getLPRoutingInfoAddition()).readFromNBT(paramNBTTagCompound.getCompoundTag("LPRoutingInformation"));
	}

	public static void renderItemTransportBox(TravelingItem item) {
		if (item.stack.hasTagCompound()) {
			if (item.stack.getTagCompound().getString("LogsitcsPipes_ITEM_ON_TRANSPORTATION").equals("YES")) {
				double scale = 0.59 / 0.6;
				LogisticsRenderPipe.boxRenderer.doRenderItem(ItemStack.EMPTY, 10, 0, 0, 0, scale, 0, 0, 0);
			}
		}
	}

	public static ItemStack handleItemSendPacket(ItemStack stack, TravelingItem item) {
		if (item.stack == null || item.stack.isEmpty()) {
			return null;
		}
		if (((ILPTravelingItemInfo) item).getLPRoutingInfoAddition() != null) {
			stack = stack.copy();
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setString("LogsitcsPipes_ITEM_ON_TRANSPORTATION", "YES");
		}
		return stack;
	}
}