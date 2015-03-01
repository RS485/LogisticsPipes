package logisticspipes.asm.td;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.routing.ItemRoutingInformation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.ducts.item.TileItemDuct;
import cofh.thermaldynamics.ducts.item.TravelingItem;

public class ThermalDynamicsHooks {
	
	public static TileEntity checkGetTileEntity(TileEntity tile, int side, TileMultiBlock source) {
		if(source instanceof TileItemDuct) {
			if(tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) tile;
				return pipe.tdPart.getInternalDuctForSide(ForgeDirection.getOrientation(side).getOpposite());
			}
		}
		return tile;
	}
	
	public static TileEntity checkGetTileEntity(TileEntity tile, byte side, TileMultiBlock source) {
		return ThermalDynamicsHooks.checkGetTileEntity(tile, (int) side, source);
	}

	public static void travelingItemToNBT(TravelingItem travelingItem, NBTTagCompound paramNBTTagCompound) {
		NBTTagCompound save = new NBTTagCompound();
		((ItemRoutingInformation)travelingItem.lpRoutingInformation).writeToNBT(save);
		paramNBTTagCompound.setTag("LPRoutingInformation", save);
	}

	public static void travelingItemNBTContructor(TravelingItem travelingItem, NBTTagCompound paramNBTTagCompound) {
		if(!paramNBTTagCompound.hasKey("LPRoutingInformation")) return;
		travelingItem.lpRoutingInformation = new ItemRoutingInformation();
		((ItemRoutingInformation)travelingItem.lpRoutingInformation).readFromNBT(paramNBTTagCompound.getCompoundTag("LPRoutingInformation"));
	}

	public static void renderItemTransportBox(TravelingItem item) {
		if(!LogisticsRenderPipe.config.isUseNewRenderer()) return;
		double scale = 0.65 / 0.6;
		LogisticsRenderPipe.boxRenderer.doRenderItem(null, 10, 0, 0, 0, scale);
	}
	
}
