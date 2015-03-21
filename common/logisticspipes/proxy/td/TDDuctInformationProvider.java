package logisticspipes.proxy.td;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.lib.util.helpers.BlockHelper;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.multiblock.Route;

public class TDDuctInformationProvider implements IPipeInformationProvider {

	private final TileItemDuct duct;
	public TDDuctInformationProvider(TileItemDuct duct) {
		this.duct = duct;
	}
	
	@Override
	public boolean isCorrect() {
		return duct != null && !duct.isInvalid() && SimpleServiceLocator.thermalDynamicsProxy.isActive();
	}
	
	@Override
	public int getX() {
		return duct.xCoord;
	}
	
	@Override
	public int getY() {
		return duct.yCoord;
	}
	
	@Override
	public int getZ() {
		return duct.zCoord;
	}
	
	@Override
	public World getWorld() {
		return duct.getWorldObj();
	}
	
	@Override
	public boolean isInitialised() {
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
	public TileEntity getTile(ForgeDirection direction) {
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
	public boolean isOutputOpen(ForgeDirection direction) {
		return duct.isSideConnected((byte) direction.ordinal());
	}
	
	@Override
	public boolean canConnect(TileEntity to, ForgeDirection direction, boolean ignoreSystemDisconnect) {
		TileEntity connection = duct.getAdjTileEntitySafe(direction.ordinal());
		if(!(connection instanceof TileTDBase)) return false;
		if(duct.isBlockedSide(direction.ordinal())) return false;
		if(connection instanceof LPItemDuct) {
			return !((LPItemDuct)connection).isLPBlockedSide(direction.getOpposite().ordinal(), ignoreSystemDisconnect);
		} else {
			return !((TileTDBase)connection).isBlockedSide(direction.getOpposite().ordinal());
		}
	}
	
	@Override
	public int getDistance() {
		return Math.max(duct.getDuctType().pathWeight, 0);
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
	public int getDistanceTo(int destinationint, ForgeDirection ignore, ItemIdentifier ident, boolean isActive, int traveled, int max, List<LPPosition> visited) {
		if(traveled >= max) return Integer.MAX_VALUE;
		IRouter destination = SimpleServiceLocator.routerManager.getRouter(destinationint);
		if(destination == null) return Integer.MAX_VALUE;
		Iterable<Route> paramIterable = duct.getCache(true).outputRoutes;
		int closesedConnection = Integer.MAX_VALUE;
		for(Route localRoute1: paramIterable) {
			if(localRoute1.endPoint instanceof LPItemDuct) {
				LPItemDuct lpDuct = (LPItemDuct) localRoute1.endPoint;
				
				if(traveled + localRoute1.pathWeight > max) continue;
				
				LPPosition pos = new LPPosition((TileEntity) lpDuct.pipe);
				if(visited.contains(pos)) continue;
				visited.add(pos);
				
				int distance = lpDuct.pipe.getDistanceTo(destinationint, ForgeDirection.getOrientation(localRoute1.pathDirections.get(localRoute1.pathDirections.size() - 1)).getOpposite(), ident, isActive, traveled + localRoute1.pathWeight, Math.min(max, closesedConnection), visited);
				
				visited.remove(pos);
				
				if(distance != Integer.MAX_VALUE && distance + localRoute1.pathWeight < closesedConnection) {
					closesedConnection = distance + localRoute1.pathWeight;
				}
			}
		}
		return closesedConnection;
	}

	@Override
	public boolean acceptItem(LPTravelingItem item, TileEntity from) {
		if(item instanceof LPTravelingItemServer) {
			LPTravelingItemServer serverItem = (LPTravelingItemServer) item;
			int id = serverItem.getInfo().destinationint;
			if(id == -1) {
				id = SimpleServiceLocator.routerManager.getIDforUUID(serverItem.getInfo().destinationUUID);
			}
			IRouter destination = SimpleServiceLocator.routerManager.getRouter(id);
			if(destination == null) return false;
			Iterable<Route> paramIterable = duct.getCache(true).outputRoutes;
			Pair<Integer, Route> closesedConnection = null;
			List<LPPosition> visited = new ArrayList<LPPosition>();
			visited.add(new LPPosition(from));
			for(Route localRoute1: paramIterable) {
				if(localRoute1.endPoint instanceof LPItemDuct) {
					LPItemDuct lpDuct = (LPItemDuct) localRoute1.endPoint;
					
					int max = Integer.MAX_VALUE;
					if(closesedConnection != null) {
						max = closesedConnection.getValue1();
					}
					
					LPPosition pos = new LPPosition((TileEntity) lpDuct.pipe);
					if(visited.contains(pos)) continue;
					visited.add(pos);
					
					int distance = lpDuct.pipe.getDistanceTo(id, ForgeDirection.getOrientation(localRoute1.pathDirections.get(localRoute1.pathDirections.size() - 1)).getOpposite(), item.getItemIdentifierStack().getItem(), serverItem.getInfo()._transportMode == TransportMode.Active, localRoute1.pathWeight, max, visited);
					
					visited.remove(pos);
					
					if(distance != Integer.MAX_VALUE && (closesedConnection == null || distance + localRoute1.pathDirections.size() < closesedConnection.getValue1())) {
						closesedConnection = new Pair<Integer, Route>(distance + localRoute1.pathWeight, localRoute1);
					}
				}
			}
			if(closesedConnection != null) {
				TravelingItem travelItem = new TravelingItem(item.getItemIdentifierStack().makeNormalStack(), duct, closesedConnection.getValue2().copy(), (byte)serverItem.output.ordinal(), (byte)1 /* Speed */);
				travelItem.lpRoutingInformation = serverItem.getInfo();
				duct.insertNewItem(travelItem);
				return true;
			}
		} else {
			return true;
		}
		return false;
	}
	
}
