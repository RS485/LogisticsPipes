package logisticspipes.asm.td;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.routing.ItemRoutingInformation;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TravelingItem;

public class ThermalDynamicsHooks {

	public static TileEntity checkGetTileEntity(TileEntity tile, int side, TileTDBase source) {
		if (source instanceof TileItemDuct) {
			if (tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) tile;
				return pipe.tdPart.getInternalDuctForSide(ForgeDirection.getOrientation(side).getOpposite());
			}
		}
		return tile;
	}

	public static void travelingItemToNBT(TravelingItem travelingItem, NBTTagCompound paramNBTTagCompound) {
		if (travelingItem.lpRoutingInformation != null) {
			NBTTagCompound save = new NBTTagCompound();
			((ItemRoutingInformation) travelingItem.lpRoutingInformation).writeToNBT(save);
			paramNBTTagCompound.setTag("LPRoutingInformation", save);
		}
	}

	public static void travelingItemNBTContructor(TravelingItem travelingItem, NBTTagCompound paramNBTTagCompound) {
		if (!paramNBTTagCompound.hasKey("LPRoutingInformation")) {
			return;
		}
		travelingItem.lpRoutingInformation = new ItemRoutingInformation();
		((ItemRoutingInformation) travelingItem.lpRoutingInformation).readFromNBT(paramNBTTagCompound.getCompoundTag("LPRoutingInformation"));
	}

	public static void renderItemTransportBox(TravelingItem item) {
		if (!LogisticsRenderPipe.config.isUseNewRenderer()) {
			return;
		}
		if (item.stack.hasTagCompound()) {
			if (item.stack.getTagCompound().getString("LogsitcsPipes_ITEM_ON_TRANSPORTATION").equals("YES")) {
				double scale = 0.65 / 0.6;
				LogisticsRenderPipe.boxRenderer.doRenderItem(null, 10, 0, 0, 0, scale);
			}
		}
	}

	public static ItemStack handleItemSendPacket(ItemStack stack, TravelingItem item) {
		if (item.stack == null) {
			return null;
		}
		if (item.lpRoutingInformation != null) {
			stack = stack.copy();
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setString("LogsitcsPipes_ITEM_ON_TRANSPORTATION", "YES");
		}
		return stack;
	}
}
