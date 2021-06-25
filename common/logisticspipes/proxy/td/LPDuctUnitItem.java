package logisticspipes.proxy.td;

import java.util.Collections;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import static logisticspipes.pipes.basic.LogisticsBlockGenericPipe.PIPE_CONN_BB;

import logisticspipes.asm.td.ILPTravelingItemInfo;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.td.subproxies.TDPart;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.ServerRouter;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import network.rs485.logisticspipes.logistics.LogisticsManager;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LPDuctUnitItem extends DuctUnitItem {

	public final LogisticsTileGenericPipe pipe;

	public LPDuctUnitItem(TileGrid parent, Duct duct, LogisticsTileGenericPipe pipe) {
		super(parent, duct);
		this.pipe = pipe;
	}

	public boolean isLPBlockedSide(int paramInt, boolean ignoreSystemDisconnect) {
		EnumFacing dir = EnumFacing.getFront(paramInt);

		AxisAlignedBB aabb = PIPE_CONN_BB.get(paramInt);
		if (SimpleServiceLocator.mcmpProxy.checkIntersectionWith(pipe, aabb)) {
			return true;
		}

		if (pipe.getTileCache() == null) {
			DoubleCoordinates coords = new DoubleCoordinates(pipe.getTile().getPos());
			if (CoordinateUtils.add(coords, dir, 1).getTileEntity(pipe.getWorld()) instanceof LogisticsTileGenericPipe) {
				return true;
			}
		} else if (pipe.getTileCache()[dir.ordinal()].getTile() instanceof LogisticsTileGenericPipe) {
			return true;
		}

		if (pipe.pipe != null && pipe.pipe.isSideBlocked(dir, ignoreSystemDisconnect)) {
			return false;
		}
		TDPart.callSuperSideBlock = true;
		boolean re = parent.isSideBlocked(paramInt);
		TDPart.callSuperSideBlock = false;
		return re;
	}

	@Override
	public boolean isOutput() {
		return true;
	}

	@Override
	public boolean isOutput(int side) {
		return true;
	}

	@Override
	public ConnectionType getConnectionType(byte side) {
		if (this.tileCache[side] == null) {
			this.tileCache[side] = new Cache(pipe, null);
		}
		return cofh.thermaldynamics.duct.ConnectionType.NORMAL;
	}

	@Override
	public boolean isNode() {
		return true;
	}

	@Override
	public int canRouteItem(@Nonnull ItemStack stack, byte directionOrdinal) {
		if (!stack.isEmpty()) {
			if (pipe.pipe.isRouted() && !((CoreRoutedPipe) pipe.pipe).stillNeedReplace()) {
				final ServerRouter serverRouter = (ServerRouter) ((CoreRoutedPipe) pipe.pipe).getRouter();
				if (LogisticsManager.INSTANCE.getDestination(stack, ItemIdentifier.get(stack), true, serverRouter, Collections.emptyList()) != null) {
					return 0;
				}
			}
		}
		return -1;
	}

	@Override
	public void transferItem(TravelingItem item) {
		ItemRoutingInformation info = (ItemRoutingInformation) ((ILPTravelingItemInfo) item).getLPRoutingInfoAddition();
		if (info != null) {
			info.setItem(ItemIdentifierStack.getFromStack(item.stack));
			LPTravelingItem.LPTravelingItemServer lpItem = new LPTravelingItem.LPTravelingItemServer(info);
			lpItem.setSpeed(info._transportMode == IRoutedItem.TransportMode.Active ? 0.3F : 0.2F);
			pipe.pipe.transport.injectItem(lpItem, EnumFacing.getFront(item.direction).getOpposite());
		} else if (item.stack != null) {
			int consumed = pipe.injectItem(item.stack, true, EnumFacing.getFront(item.direction).getOpposite());
			item.stack.shrink(consumed);
			if (item.stack.getCount() > 0) {
				pipe.pipe.transport._itemBuffer.add(new Triplet<>(ItemIdentifierStack
						.getFromStack(item.stack), new Pair<>(20 * 2, 0), null));
			}
		}
	}
}
