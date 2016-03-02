package logisticspipes.proxy.td;

import java.util.ArrayList;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.multiblock.IMultiBlock;

public class LPItemDuct extends TileItemDuct {

	public final LogisticsTileGenericPipe pipe;
	public final ForgeDirection dir;

	public LPItemDuct(LogisticsTileGenericPipe pipe, ForgeDirection orientation) {
		this.pipe = pipe;
		dir = orientation;
	}

	@Override
	public RouteInfo canRouteItem(ItemStack arg0) {
		if (arg0 != null) {
			if (pipe.pipe.isRoutedPipe() && !((CoreRoutedPipe)pipe.pipe).stillNeedReplace()) {
				if (SimpleServiceLocator.logisticsManager.hasDestination(ItemIdentifier.get(arg0), true, ((CoreRoutedPipe) pipe.pipe).getRouterId(), new ArrayList<Integer>()) != null) {
					return new RouteInfo(0, (byte) dir.getOpposite().ordinal());
				}
			}
		}
		return TileItemDuct.noRoute;
	}

	@Override
	public void transferItem(TravelingItem item) {
		ItemRoutingInformation info = (ItemRoutingInformation) item.lpRoutingInformation;
		if (info != null) {
			info.setItem(ItemIdentifierStack.getFromStack(item.stack));
			LPTravelingItemServer lpItem = new LPTravelingItemServer(info);
			lpItem.setSpeed(info._transportMode == TransportMode.Active ? 0.3F : 0.2F);
			pipe.pipe.transport.injectItem(lpItem, ForgeDirection.getOrientation(item.direction));
		} else if (item.stack != null) {
			int consumed = pipe.injectItem(item.stack, true, dir);
			item.stack.stackSize -= consumed;
			if (item.stack.stackSize > 0) {
				pipe.pipe.transport._itemBuffer.add(new Triplet<ItemIdentifierStack, Pair<Integer, Integer>, LPTravelingItemServer>(ItemIdentifierStack.getFromStack(item.stack), new Pair<Integer, Integer>(20 * 2, 0), null));
			}
		}
	}

	@Override
	public boolean isBlockedSide(int paramInt) {
		return isLPBlockedSide(paramInt, false);
	}

	public boolean isLPBlockedSide(int paramInt, boolean ignoreSystemDisconnect) {
		ForgeDirection dir = ForgeDirection.getOrientation(paramInt);
		if (pipe.tilePart.hasBlockingPluggable(dir)) {
			return true;
		}
		if (pipe.pipe != null && pipe.pipe.isSideBlocked(dir, ignoreSystemDisconnect)) {
			return false;
		}
		return super.isBlockedSide(paramInt);
	}

	@Override
	public Duct getDuctType() {
		if (duct == null) {
			duct = TDDucts.itemBasic;
		}
		return duct;
	}

	@Override
	public void handleSideUpdate(int paramInt) {
		super.handleSideUpdate(paramInt);
		isOutput = true;
	}

	@Override
	public IMultiBlock getConnectedSide(byte paramByte) {
		if (ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getConnectedSide(paramByte);
	}

	@Override
	public NeighborTypes getCachedSideType(byte paramByte) {
		if (ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getCachedSideType(paramByte);
	}

	@Override
	public ConnectionTypes getConnectionType(byte paramByte) {
		if (ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getConnectionType(paramByte);
	}

	@Override
	public IMultiBlock getCachedTile(byte paramByte) {
		if (ForgeDirection.getOrientation(paramByte) != dir) {
			return null;
		}
		return super.getCachedTile(paramByte);
	}

	@Override
	public TileEntity getAdjTileEntitySafe(int ordinal) {
		if (ForgeDirection.getOrientation(ordinal) != dir) {
			return null;
		}
		return super.getAdjTileEntitySafe(ordinal);
	}
}
