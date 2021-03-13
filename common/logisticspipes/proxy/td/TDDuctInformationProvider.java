package logisticspipes.proxy.td;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import cofh.core.util.helpers.BlockHelper;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.GridItem;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.IDuctHolder;
import cofh.thermaldynamics.duct.tiles.TileDuctItem;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;

import logisticspipes.asm.td.ILPTravelingItemInfo;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.routing.pathfinder.IRouteProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import network.rs485.logisticspipes.connection.ConnectionType;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class TDDuctInformationProvider implements IPipeInformationProvider, IRouteProvider {

	private final TileDuctItem duct;

	public TDDuctInformationProvider(TileDuctItem duct) {
		this.duct = duct;
	}

	@Override
	public boolean isCorrect(ConnectionType type) {
		return duct != null && !duct.isInvalid() && SimpleServiceLocator.thermalDynamicsProxy.isActive();
	}

	@Override
	public int getX() {
		return duct.getPos().getX();
	}

	@Override
	public int getY() {
		return duct.getPos().getY();
	}

	@Override
	public int getZ() {
		return duct.getPos().getZ();
	}

	@Override
	public World getWorld() {
		return duct.getWorld();
	}

	@Override
	public boolean isRouterInitialized() {
		return !duct.isInvalid();
	}

	@Override
	public boolean isRoutingPipe() {
		return false;
	}

	@Override
	public CoreRoutedPipe getRoutingPipe() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TileEntity getNextConnectedTile(EnumFacing direction) {
		return BlockHelper.getAdjacentTileEntity(duct, direction);
	}

	@Override
	public boolean isFirewallPipe() {
		return false;
	}

	@Override
	public IFilter getFirewallFilter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TileEntity getTile() {
		return duct;
	}

	@Override
	public boolean divideNetwork() {
		return false;
	}

	@Override
	public boolean powerOnly() {
		return false;
	}

	@Override
	public boolean isOnewayPipe() {
		return false;
	}

	@Override
	public boolean isOutputClosed(EnumFacing direction) {
		final DuctUnitItem duct = this.duct.getDuct(DuctToken.ITEMS);
		if (duct == null) return true;
		return !duct.isSideConnected((byte) direction.ordinal());
	}

	@Override
	public boolean canConnect(TileEntity to, EnumFacing direction, boolean ignoreSystemDisconnect) {
		TileEntity connection = CoordinateUtils.add(new DoubleCoordinates(duct.getPos()), direction).getTileEntity(duct.getWorld());
		if (duct.isSideBlocked((byte) direction.ordinal())) {
			return false;
		}
		if (!(connection instanceof IDuctHolder)) {
			return false;
		}
		DuctUnitItem connectedDuct = ((IDuctHolder) connection).getDuct(DuctToken.ITEMS);
		if (connectedDuct == null) {
			return false;
		} else if (connectedDuct instanceof LPDuctUnitItem) {
			return !((LPDuctUnitItem) connectedDuct).isLPBlockedSide(direction.getOpposite().ordinal(), ignoreSystemDisconnect);
		} else {
			return !connectedDuct.parent.isSideBlocked(direction.getOpposite().ordinal());
		}
	}

	@Nonnull
	private DuctUnitItem getDuct() {
		return Objects.requireNonNull(this.duct.getDuct(DuctToken.ITEMS), "duct must be non-null");
	}

	@Override
	public double getDistance() {
		return Math.max(getDuct().getDuctLength(), 0);
	}

	@Override
	public double getDistanceWeight() {
		return Math.max(getDuct().getWeight(), 0);
	}

	@Override
	public boolean isItemPipe() {
		return true;
	}

	@Override
	public boolean isFluidPipe() {
		return false;
	}

	@Override
	public boolean isPowerPipe() {
		return false;
	}

	@Override
	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double traveled, double max,
			List<DoubleCoordinates> visited) {
		if (traveled >= max) {
			return Integer.MAX_VALUE;
		}
		IRouter destination = SimpleServiceLocator.routerManager.getRouter(destinationint);
		if (destination == null) {
			return Integer.MAX_VALUE;
		}
		LinkedList<Route<DuctUnitItem, GridItem>> paramIterable = getDuct().getCache(true).outputRoutes;
		double closesedConnection = Integer.MAX_VALUE;
		for (Route<DuctUnitItem, GridItem> localRoute1 : paramIterable) {
			if (localRoute1.endPoint instanceof LPDuctUnitItem) {
				LPDuctUnitItem lpDuct = (LPDuctUnitItem) localRoute1.endPoint;

				if (traveled + localRoute1.pathWeight > max) {
					continue;
				}

				DoubleCoordinates pos = new DoubleCoordinates((TileEntity) lpDuct.pipe);
				if (visited.contains(pos)) {
					continue;
				}
				visited.add(pos);

				double distance = lpDuct.pipe
						.getDistanceTo(destinationint, EnumFacing.getFront(localRoute1.pathDirections.get(localRoute1.pathDirections.size() - 1)).getOpposite(),
								ident, isActive, traveled + localRoute1.pathWeight, Math.min(max, closesedConnection), visited);

				visited.remove(pos);

				if (distance != Integer.MAX_VALUE && distance + localRoute1.pathWeight < closesedConnection) {
					closesedConnection = distance + localRoute1.pathWeight;
				}
			}
		}
		return closesedConnection;
	}

	@Override
	public boolean acceptItem(LPTravelingItem item, TileEntity from) {
		if (item instanceof LPTravelingItemServer) {
			LPTravelingItemServer serverItem = (LPTravelingItemServer) item;
			int id = serverItem.getInfo().destinationint;
			if (id == -1) {
				id = SimpleServiceLocator.routerManager.getIDforUUID(serverItem.getInfo().destinationUUID);
			}
			IRouter destination = SimpleServiceLocator.routerManager.getRouter(id);
			if (destination == null) {
				return false;
			}
			RouteCache<DuctUnitItem, GridItem> routes = getDuct().getCache(true);
			Iterable<Route<DuctUnitItem, GridItem>> paramIterable = routes.outputRoutes;
			Route<DuctUnitItem, GridItem> route = null;
			Object cache = null;
			Triplet<Integer, ItemIdentifier, Boolean> key = new Triplet<>(id, item.getItemIdentifierStack()
					.getItem(), serverItem.getInfo()._transportMode == TransportMode.Active);
			if (duct instanceof ILPTEInformation && ((ILPTEInformation) duct).getObject() != null) {
				cache = ((ILPTEInformation) duct).getObject().getCacheHolder().getCacheFor(CacheTypes.Routing, key);
			}
			if (cache instanceof Route) {
				//noinspection unchecked
				route = (Route<DuctUnitItem, GridItem>) cache;
				if (!routes.outputRoutes.contains(route)) {
					route = null;
				}
			}
			if (route == null) {
				Pair<Double, Route<DuctUnitItem, GridItem>> closesedConnection = null;
				List<DoubleCoordinates> visited = new ArrayList<>();
				visited.add(new DoubleCoordinates(from));
				for (Route<DuctUnitItem, GridItem> localRoute1 : paramIterable) {
					if (localRoute1.endPoint instanceof LPDuctUnitItem) {
						LPDuctUnitItem lpDuct = (LPDuctUnitItem) localRoute1.endPoint;

						double max = Integer.MAX_VALUE;
						if (closesedConnection != null) {
							max = closesedConnection.getValue1();
						}

						DoubleCoordinates pos = new DoubleCoordinates((TileEntity) lpDuct.pipe);
						if (visited.contains(pos)) {
							continue;
						}
						visited.add(pos);

						double distance = lpDuct.pipe
								.getDistanceTo(id, EnumFacing.getFront(localRoute1.pathDirections.get(localRoute1.pathDirections.size() - 1)).getOpposite(),
										item.getItemIdentifierStack().getItem(), serverItem.getInfo()._transportMode == TransportMode.Active,
										localRoute1.pathWeight, max,
										visited);

						visited.remove(pos);

						if (distance != Integer.MAX_VALUE && (closesedConnection == null || distance + localRoute1.pathDirections.size() < closesedConnection
								.getValue1())) {
							closesedConnection = new Pair<>(distance + localRoute1.pathWeight, localRoute1);
						}
					}
				}
				if (closesedConnection != null) {
					route = closesedConnection.getValue2();
				}
			}
			if (route != null) {
				if (duct instanceof ILPTEInformation && ((ILPTEInformation) duct).getObject() != null) {
					((ILPTEInformation) duct).getObject().getCacheHolder().setCache(CacheTypes.Routing, key, route);
				}
				TravelingItem travelItem = new TravelingItem(item.getItemIdentifierStack().makeNormalStack(), getDuct(), route.copy(), (byte) serverItem.output.ordinal(), (byte) 1 /* Speed */);
				//noinspection ConstantConditions
				((ILPTravelingItemInfo) travelItem).setLPRoutingInfoAddition(serverItem.getInfo());
				getDuct().insertNewItem(travelItem);
				return true;
			}
		} else {
			return true;
		}
		return false;
	}

	@Override
	public void refreshTileCacheOnSide(EnumFacing side) {
		if (duct.getDuct(DuctToken.ITEMS) == null || getDuct().getGrid() == null) return;
		getDuct().getGrid().destroyAndRecreate();
	}

	@Override
	public boolean isMultiBlock() {
		return false;
	}

	@Override
	public Stream<TileEntity> getPartsOfPipe() {
		return Stream.empty();
	}

	@Override
	public List<RouteInfo> getConnectedPipes(EnumFacing from) {
		List<RouteInfo> list = new ArrayList<>();
		if (duct.getDuct(DuctToken.ITEMS) == null || getDuct().getGrid() == null) return null;
		LinkedList<Route<DuctUnitItem, GridItem>> paramIterable = getDuct().getCache(true).outputRoutes;
		for (Route<DuctUnitItem, GridItem> localRoute1 : paramIterable) {
			if (localRoute1.endPoint instanceof LPDuctUnitItem) {
				LPDuctUnitItem lpDuct = (LPDuctUnitItem) localRoute1.endPoint;
				list.add(new RouteInfo(lpDuct.pipe, localRoute1.pathWeight,
						EnumFacing.getFront(localRoute1.pathDirections.get(localRoute1.pathDirections.size() - 1))));
			}
		}
		return list;
	}
}