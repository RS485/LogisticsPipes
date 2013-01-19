package logisticspipes.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.utils.ItemIdentifier;
import net.minecraftforge.common.ForgeDirection;

public class FilteringRouter extends ServerRouter implements IFilteringRouter {
	
	private ForgeDirection side;
	//private boolean init = false;
	
	public FilteringRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord, ForgeDirection dir) {
		super(id, dimension, xCoord, yCoord, zCoord);
		this.side = dir;
	}
	/*
	protected void recheckAdjacent() {
		if(ForgeDirection.UNKNOWN.equals(side)) {
			if (!init) {
				HashMap<IRouter, ExitRoute> adjacentRouter = new HashMap<IRouter, ExitRoute>();
				for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
					adjacentRouter.put(getPipe().getRouter(dir), new ExitRoute(dir, dir.getOpposite(), 0, EnumSet.allOf(PipeRoutingConnectionType.class)));
				}
				_adjacentRouter = adjacentRouter;
				_adjacent = new HashMap<RoutedPipe, ExitRoute>(0);
				_powerAdjacent = new ArrayList<ILogisticsPowerProvider>(0);
				init = true;
				_blockNeedsUpdate = true;
				SendNewLSA();
			}
		} else {
			super.recheckAdjacent();
		}
	}
	
	protected void SendNewLSA() {
		HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> neighboursWithMetric = new HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>>();
		for (RoutedPipe adjacent : _adjacent.keySet()){
			neighboursWithMetric.put(adjacent.getRouter(_adjacent.get(adjacent).insertOrientation), new Pair<Integer, EnumSet<PipeRoutingConnectionType>>(_adjacent.get(adjacent).metric, _adjacent.get(adjacent).connectionDetails));
		}
		SharedLSADatabasewriteLock.lock();
		_myLsa.neighboursWithMetric = neighboursWithMetric;
		_myLsa.power = new ArrayList<ILogisticsPowerProvider>(0);
		_LSDVersion++;
		SharedLSADatabasewriteLock.unlock();
	}
	*/
	@Override
	public List<SearchNode> getRouters() {
		if(LogisticsPipes.DEBUG && ForgeDirection.UNKNOWN.equals(side)) {
			throw new UnsupportedOperationException();
		}
		if(this.getPipe() instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall)this.getPipe()).getRouters(this);
		}
		return new ArrayList<SearchNode>();
	}

	@Override
	public IFilter getFilter() {
		if(LogisticsPipes.DEBUG && ForgeDirection.UNKNOWN.equals(side)) {
			throw new UnsupportedOperationException();
		}
		if(this.getPipe() instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall)this.getPipe()).getFilter(this.getId());
		}
		return new IFilter() {
			@Override public UUID getUUID() {return UUID.randomUUID();}
			@Override public boolean isBlocked() {return false;}
			@Override public List<ItemIdentifier> getFilteredItems() {return new ArrayList<ItemIdentifier>();}
			@Override public boolean blockProvider() {return false;}
			@Override public boolean blockCrafting() {return false;}
		};
	}

	@Override
	public boolean idIdforOtherSide(UUID id) {
		if(this.getPipe() instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall)this.getPipe()).idIdforOtherSide(id);
		}
		return false;
	}
}
