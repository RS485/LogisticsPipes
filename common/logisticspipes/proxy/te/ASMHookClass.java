package logisticspipes.proxy.te;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermalexpansion.block.conduit.BlockConduit;
import thermalexpansion.part.conduit.ConduitBase;
import thermalexpansion.part.conduit.item.ConduitItem;

public class ASMHookClass {
	private static void handleTileCheck(ConduitBase conduit, int side, TileEntity tile) {
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).canTEConduitConnect(conduit, side)) {
			conduit.conduitCache[side] = ((LogisticsTileGenericPipe)tile).getTEConduit(ForgeDirection.VALID_DIRECTIONS[side].getOpposite().ordinal());
			conduit.sideType[side] = 1;
		}
	}
	
	public static void handleOnNeighborChanged(ConduitBase conduit, int side, TileEntity tile) {
		if(conduit.isItemConduit()) {
			handleTileCheck(conduit, side, tile);
		}
	}
	
	public static void handleOnNeighborTileChanged(ConduitBase conduit, int side, TileEntity tile) {
		if(conduit.isItemConduit()) {
			handleTileCheck(conduit, side, tile);
		}
	}
	
	public static void handleOnAdded(ConduitBase conduit, int side, TileEntity tile) {
		if(conduit.isItemConduit()) {
			handleTileCheck(conduit, side, tile);
		}
	}
	
	public static void handleOnPartChanged(ConduitBase conduit, TileEntity tile, int side) {
		if(conduit.isItemConduit()) {
			if(conduit.passOcclusionTest(side)) {
				handleTileCheck(conduit, side, tile);
			}
		}
	}
	
	public static void handleGetConduit(ConduitBase conduit, int side, TileEntity tile) {
		if(conduit.isItemConduit()) {
			if(tile instanceof LogisticsTileGenericPipe) {
				conduit.conduitCache[side] = ((LogisticsTileGenericPipe)tile).getTEConduit(side);
			}
		}
	}

	public static int getTEPipeRenderMode(ConduitItem conduit, int side) {
		if(conduit.sideType[side] == 1) {
			if(conduit.conduitCache[side] instanceof LPConduitItem || conduit.getConduit(side) instanceof LPConduitItem) {
				return BlockConduit.ConnectionTypes.FLUID_NORMAL.ordinal();
			}
		}
		return -1;
	}

	public static void handleOnRemoved(ConduitBase conduit) {
		if(MainProxy.isClient()) return;
		if(conduit.isItemConduit()) {
			ConduitItem itemC = conduit.getConduitItem();
			for(TravelingItem item:itemC.myItems) {
				if(item.routedLPInfo != null) {
					LPTravelingItemServer lpItem = new LPTravelingItemServer(item.routedLPInfo);
					lpItem.itemWasLost();
				}
			}
		}
	}

	public static void handleTETravelingItemSave(TravelingItem item, NBTTagCompound tag) {
		if(item.routedLPInfo != null) {
			NBTTagCompound nbt = new NBTTagCompound();
			item.routedLPInfo.writeToNBT(nbt);
			tag.setTag("LPRoutingInformation", nbt);
		}
	}
	
	public static void handleTETravelingItemLoad(TravelingItem item, NBTTagCompound tag) {
		if(tag.hasKey("LPRoutingInformation")) {
			item.routedLPInfo = new ItemRoutingInformation();
			item.routedLPInfo.readFromNBT(tag.getCompoundTag("LPRoutingInformation"));
		}
	}
}
